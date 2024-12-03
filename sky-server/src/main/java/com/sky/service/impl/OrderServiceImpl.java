package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
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
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
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
    @Autowired
    private WebSocketServer webSocketServer;


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

        //通过websocket向客户端浏览器推送消息 type orderid content
        Map map  = new HashMap();
        map.put("type", 1); // 1表示来单提醒，2表示客户催单
        map.put("orderid", ordersDB.getId());
        map.put("content", "" + outTradeNo);

        String jsonString = JSON.toJSONString(map);
        //通过websocket向商家端推送消息
        webSocketServer.sendToAllClient(jsonString);
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


    /**
     * 用户取消订单
     * @param id
     */
    @Override
    public void userCancelById(Long id) throws Exception {
        //根据订单查询订单信息
        Orders ordersDB = orderMapper.getByOrderId(id);

        //校验订单是否存在
        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //根据订单状态进行不同处理
        if(ordersDB.getStatus() > Orders.TO_BE_CONFIRMED){//订单状态大于2（待接单）
            //订单状态为3（已接单）或4（派送中），要与商家沟通
            if(ordersDB.getStatus() == Orders.CONFIRMED || ordersDB.getStatus() == Orders.DELIVERY_IN_PROGRESS){
                throw new OrderBusinessException("请与商家沟通后取消订单");
            }else{//其余状态为订单状态错误
                throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
            }
        }

        //创建一个新的订单对象，用更新数据
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());//设置订单ID

        //待接单状态需要退款
        if(ordersDB.getStatus() == Orders.TO_BE_CONFIRMED){
            //调用微信支付的退款接口完成退款
            weChatPayUtil.refund(
                    ordersDB.getNumber(), //商户订单号
                    ordersDB.getNumber(), //商户退款单号，与订单号一致
                    new BigDecimal(0.01), //退款金额
                    new BigDecimal(0.01) //原订单金额
            );

            //将订单的支付状态更改为退款
            orders.setPayStatus(Orders.REFUND);
        }

        //更新状态为已取消
        orders.setStatus(Orders.CANCELLED);
        //设置取消原因为“用户取消”
        orders.setCancelReason("用户取消");
        //设置取消时间为当前时间
        orders.setCancelTime(LocalDateTime.now());


        //更新到数据库
        orderMapper.update(orders);
    }

    @Override
    public void repetition(Long id) {
        //根据订单id查询该订单的详细信息（订单详情表）（包括菜品、数量、套餐）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderID(id);

        //使用stream流操作，将订单详情信息转换为购物车对象列表
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 复制订单详情中的属性到购物车对象
            // 使用 BeanUtils.copyProperties 方法快速复制属性，但排除 "id" 字段
            // 因为购物车对象的 "id" 应由数据库自动生成
            BeanUtils.copyProperties(x, shoppingCart, "id");

            // 设置购物车所属用户ID
            shoppingCart.setUserId(BaseContext.getCurrentId());

            // 设置购物车记录的创建时间为当前时间
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart; // 返回转换后的购物车对象
        }).collect(Collectors.toList()); // 将流结果收集为列表

        shoppingCartMapper.insertBatch(shoppingCartList);

    }

    /**
     * 订单搜索，条件查询，分页展示
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionalSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        //使用PageHelper插件分页查询
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> pages = orderMapper.pageQuery(ordersPageQueryDTO);

        //部分订单状态，需要额外返回订单菜品信息，调用getOrderVOList方法将Orders转化为OrderVO返回给前端
        List<OrderVO> orderVOList = getOrderVOList(pages);

        return new PageResult(pages.getTotalElements(),orderVOList);
    }

    /**
     * 各个状态的订单数量统计
     *
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed =orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);

        return orderStatisticsVO;
    }

    /**
     * 接单
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        //根据订单号查询订单
        Orders ordersDB = orderMapper.getByOrderId(ordersRejectionDTO.getId());

        //只有订单 存在且 处于“待接单”状态时可以执行拒单操作
        if(ordersDB == null || ordersDB.getStatus() != Orders.TO_BE_CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //获取订单的支付状态
        Integer payStatus = ordersDB.getPayStatus();

        //如果用户已经完成了支付，需要为用户退款
        if(payStatus == Orders.PAID){
            String refund = weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01)
            );
            log.info("申请退款：{}", refund);
        }

        //创建Orders对象，根据订单id更新订单状态，拒单原因，拒单时间
        Orders orders = new Orders();
        orders.setId(ordersRejectionDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);

    }


    /**
     * 取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {
        //根据订单号查询出要取消的订单
        Orders ordersDB = orderMapper.getByOrderId(ordersCancelDTO.getId());

        //如果用户已经完成了支付，需要为用户退款
        Integer payStatus = ordersDB.getPayStatus();
        if(payStatus == Orders.PAID){
            String refund = weChatPayUtil.refund(
                    ordersDB.getNumber(),
                    ordersDB.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01)
            );
            log.info("申请退款：{}", refund);
        }

        //创建一个Orders对象，根据订单id更新订单状态、取消原因、取消时间
        Orders orders = new Orders();
        orders.setId(ordersCancelDTO.getId());
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason(ordersCancelDTO.getCancelReason());
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 派送订单
     *
     * @param id
     */
    public void delivery(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getByOrderId(id);

        // 校验订单是否存在，并且状态为3
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为派送中
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);

        orderMapper.update(orders);
    }

    /**
     * 完成订单
     *
     * @param id
     */
    public void complete(Long id) {
        // 根据id查询订单
        Orders ordersDB = orderMapper.getByOrderId(id);

        // 校验订单是否存在，并且状态为4
        if (ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        // 更新订单状态,状态转为完成
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 用户催单
     *
     * @param id
     */
    public void reminder(Long id) {
        // 查询订单是否存在
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        //基于WebSocket实现催单
        Map map = new HashMap();
        map.put("type", 2);//2代表用户催单
        map.put("orderId", id);
        map.put("content", "订单号：" + orders.getNumber());
        webSocketServer.sendToAllClient(JSON.toJSONString(map));
    }

    private List<OrderVO> getOrderVOList(Page<Orders> pages){
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = pages.getContent();
        if(!CollectionUtils.isEmpty(ordersList)){
            for (Orders orders : ordersList) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders, orderVO);

                //判断是否需要添加菜品详细信息
                if(orders.getStatus() == Orders.TO_BE_CONFIRMED || orders.getStatus() == Orders.DELIVERY_IN_PROGRESS ||orders.getStatus()==Orders.CONFIRMED){
                    //调用getOrderDishStr函数拼接菜品信息字符串
                    String orderDishes = getOrderDishStr(orders);
                    orderVO.setOrderDishes(orderDishes);

                }

                orderVOList.add(orderVO);
            }
        }

        return orderVOList;

    }

    private String getOrderDishStr(Orders orders){
        //查询订单菜品详细信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderID(orders.getId());


        //将每条订单菜品信息拼接成字符串（格式：宫保鸡丁*3：）
        List<String> orderDishStr = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber();
            return orderDish;
        }).collect(Collectors.toList());

        //将所有信息拼接在一起
        return String.join(";", orderDishStr);
    }




}
