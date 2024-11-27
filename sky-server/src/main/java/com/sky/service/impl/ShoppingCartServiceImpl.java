package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车的具体业务逻辑
     *
     * @param shoppingCartDTO
     */
    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        // 创建购物车对象，用于存储当前商品的信息
        ShoppingCart shoppingCart = new ShoppingCart();
        // 将传入的DTO对象的属性复制到ShoppingCart对象中
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        // 获取当前用户的ID（假设是通过上下文工具类获取的）
        Long userId = BaseContext.getCurrentId();
        // 将用户ID设置到购物车对象中，确保与当前用户关联，查询当前用户的购物车，一用户一车。
        shoppingCart.setUserId(userId);

        //------------------------------------------------------------------------

        // 根据商品信息查询当前用户的购物车中是否已存在相同的商品
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        //------------------------------------------------------------------------

        // 判断查询结果是否为空，即是否已有相同商品在购物车中
        if (list != null && !list.isEmpty()) {
            // 如果存在，取出第一个匹配的商品
            ShoppingCart cart = list.get(0);
            // 将该商品的数量加1
            cart.setNumber(cart.getNumber() + 1);
            // 更新数据库中该商品的数量
            shoppingCartMapper.updateNumberById(cart);
        } else {
            // 如果购物车中没有相同商品，则新增一条记录

            // 获取当前添加的商品类型（菜品或套餐）
            Long dishId = shoppingCartDTO.getDishId();

            if (dishId != null) {
                // 如果是菜品类型，根据菜品ID查询菜品详细信息
                Dish dish = dishMapper.getById(dishId);
                // 将菜品信息填充到购物车对象中
                shoppingCart.setName(dish.getName());             // 设置菜品名称
                shoppingCart.setImage(dish.getImage());           // 设置菜品图片
                shoppingCart.setAmount(dish.getPrice());          // 设置菜品价格

            } else {
                // 如果是套餐类型，根据套餐ID查询套餐详细信息
                Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
                // 将套餐信息填充到购物车对象中
                shoppingCart.setName(setmeal.getName());          // 设置套餐名称
                shoppingCart.setImage(setmeal.getImage());        // 设置套餐图片
                shoppingCart.setAmount(setmeal.getPrice());       // 设置套餐价格

            }
            // 设置初始商品数量为1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());  // 设置创建时间


            // 将新的购物车记录插入数据库
            shoppingCartMapper.insert(shoppingCart);
        }
    }


    /**
     * 查看购物车
     *
     * @return
     */
    @Override
    public List<ShoppingCart> showShoppingCart() {
        return shoppingCartMapper.list(ShoppingCart.builder().userId(BaseContext.getCurrentId()).build());
    }

    /**
     * 清空购物车
     */
    @Override
    public void cleanShoppingCart() {
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }

    /**
     * 删除购物车中一个商品
     *
     * @param shoppingCartDTO
     */
    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shop = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shop);
        List<ShoppingCart> list = shoppingCartMapper.list(shop);
        if (list != null && !list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            if (cart.getNumber() == 1) {
                shoppingCartMapper.deleteById(cart.getId());
            } else {
                cart.setNumber(cart.getNumber() - 1);
                shoppingCartMapper.updateNumberById(cart);
            }
        }
    }
}
