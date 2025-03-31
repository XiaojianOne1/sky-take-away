package com.sky.controller.user;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.webSocket.WebSocketServer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName  OrderController
 * Author  xiaojianOne
 * Date  2025/3/28 13:59
 * Version  17.0
 */
/*
 * 　　┏┓　　　┏┓+ +
 * 　┏┛┻━━━┛┻┓ + +
 * 　┃　　　　　　　┃
 * 　┃　　　━　　　┃ ++ + + +
 *  ████━████ ┃+
 * 　┃　　　　　　　┃ +
 * 　┃　　　┻　　　┃
 * 　┃　　　　　　　┃ + +
 * 　┗━┓　　　┏━┛
 * 　　　┃　　　┃
 * 　　　┃　　　┃ + + + +
 * 　　　┃　　　┃
 * 　　　┃　　　┃ +                神兽保佑
 * 　　　┃　　　┃                  代码无bug
 * 　　　┃　　　┃　　+
 * 　　　┃　 　　┗━━━┓ + +
 * 　　　┃ 　　　　　　　┣┓
 * 　　　┃ 　　　　　　　┏┛
 * 　　　┗┓┓┏━┳┓┏┛ + + + +
 * 　　　　┃┫┫　┃┫┫
 * 　　　　┗┻┛　┗┻┛+ + + +
 */
@RestController(value = "userOrderController")
@RequestMapping("/user/order")
@Api(tags = "用户订单相关接口")
@Slf4j
public class OrderController {

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderService orderService;

    /**
     * 用户下单
     * @param orderSubmitDTO
     * @return
     */
    @PostMapping("/submit")
    @ApiOperation("用户下单")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO orderSubmitDTO){

        log.info("用户下单参数为:{}",orderSubmitDTO);

        OrderSubmitVO orderSubmitVO = orderService.submitOrder(orderSubmitDTO);

        return Result.success(orderSubmitVO);
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    @PutMapping("/payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {

        log.info("订单支付：{}", ordersPaymentDTO);

        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);

        log.info("生成预支付交易单：{}", orderPaymentVO);

        return Result.success(orderPaymentVO);

       /*
        Orders orders = new Orders();
        orders.setUserId(BaseContext.getCurrentId());
        orders.setPayStatus(Orders.PAID);
        orders.setStatus(Orders.TO_BE_CONFIRMED);
        orders.setNumber(ordersPaymentDTO.getOrderNumber());
        orders.setCheckoutTime(LocalDateTime.now());

        orderMapper.updateStatus(orders);

        return Result.success("支付成功,但功能未开发");*/
    }


    /**
     * 历史订单查询
     * @param page
     * @param pageSize
     * @param status   订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
     * @return
     */
    @GetMapping("/historyOrders")
    @ApiOperation("历史订单查询")
    public Result<PageResult> page(int page, int pageSize, Integer status) {

        PageResult pageResult = orderService.pageQueryUser(page, pageSize, status);
        return Result.success(pageResult);
    }


    /**
     * 查询订单详情
     * @param id
     * @return
     */
    @GetMapping("/orderDetail/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> details(@PathVariable("id") Long id) {
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
    }


    /**
     * 用户取消订单
     *
     * @return
     */
    @PutMapping("/cancel/{id}")
    @ApiOperation("取消订单")
    public Result cancel(@PathVariable("id") Long id) throws Exception {
        orderService.userCancelById(id);
        return Result.success();
    }


    /**
     * 再来一单
     * 再来一单就是将原订单中的商品重新加入到购物车中
     * @param id
     * @return
     */
    @PostMapping("/repetition/{id}")
    @ApiOperation("再来一单")
    public Result repetition(@PathVariable Long id) {
        orderService.repetition(id);
        return Result.success();
    }


    /**
     * 订单催单
     * @param id
     * @return
     */
    @GetMapping("/reminder/{id}")
    @ApiOperation("订单催单")
    public Result reminder(@PathVariable("id") Long id){

        orderService.reminder(id);
        return Result.success();
    }

}
