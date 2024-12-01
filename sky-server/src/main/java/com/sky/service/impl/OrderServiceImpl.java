package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;


    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Transactional //事务
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

        // 1. 处理各种业务异常（地址簿为空、购物车数据为空）
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            //抛出业务异常
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //查询当前用户的购物车数据
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(ShoppingCart.builder().id(userId).build());
        if(shoppingCartList == null || shoppingCartList.size() == 0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //其实前端都会进行校验。


        // 2. 向订单表中插入1调数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(BaseContext.getCurrentId());

        orderMapper.insert(orders);


        // 3. 向订单明细表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart shoppingCart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart,orderDetail);
            orderDetail.setOrderId(orders.getId()); //设置当前订单明细表关联的订单表的id
            orderDetailList.add(orderDetail);
        }
        //采用批量插入n条数据的操作
        orderDetailMapper.insertBatch(orderDetailList);


        // 4. 清空当前用户的购物车数据
        shoppingCartMapper.deleteByUserId(userId);


        // 5. 封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
        return orderSubmitVO;
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }


    /**
     * 用户端分页查询历史订单
     * @param page
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult pageQueryUser(int page, int pageSize, Integer status) {
        //开始分页，指定页码和每页记录数
        PageHelper.startPage(page, pageSize);

        //创建查询条件对象，用于封装查询参数
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId()); //查询当前用户的订单
        ordersPageQueryDTO.setStatus(status); //设置订单状态

        //执行查询，返回一个 Page 对象，包含分页数据的总记录数还有一个列表List<Orders>，列表内元素是Orders（从orders表中查询到的）
        Page<Orders> page1 = orderMapper.pageQuery(ordersPageQueryDTO);

        //创建一个存放结果的列表————最终结果要求List<OrderVO>，list列表中存放的是VO对象
        List<OrderVO> list = new ArrayList<>();

        //判断查询结果是否为空
        if(page1 != null && page1.getTotalElements() > 0){
            //遍历查询结果获取每一个订单
            for (Orders orders : page1) {
                Long orderId = orders.getId(); //获取订单ID

                //根据订单号，在订单明细表中查询订单明细数据
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderID(orderId);

                //创建VO对象，用于封装订单及其明细信息
                OrderVO orderVO = new OrderVO();
                //VO对象继承Orders对象，所有先将其全部信息拷贝过去
                BeanUtils.copyProperties(orders, orderVO); //赋值订单的基本信息到orderVO对象中
                orderVO.setOrderDetailList(orderDetails); //再添加订单明细数据到VO对象中
                list.add(orderVO);
            }
        }
        return new PageResult(page1.getTotalElements(), list);
    }


    /**
     * 查询订单详情
     * @param id
     */
    @Override
    public OrderVO details(Long id) {
        //根据id查询订单
        Orders orders = orderMapper.getByOrderId(id);

        //查询订单明细表
        List<OrderDetail> orderDetaiList = orderDetailMapper.getByOrderID(id);

        //封装VO返回对象
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetaiList);

        return orderVO;

    }


}
