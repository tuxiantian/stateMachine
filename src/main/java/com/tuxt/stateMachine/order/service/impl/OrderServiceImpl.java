package com.tuxt.stateMachine.order.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tuxt.stateMachine.order.CommonConstants;
import com.tuxt.stateMachine.order.OrderStatus;
import com.tuxt.stateMachine.order.OrderStatusChangeEvent;
import com.tuxt.stateMachine.order.entity.Order;
import com.tuxt.stateMachine.order.mapper.OrderMapper;
import com.tuxt.stateMachine.order.service.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
* @author tuxiantian
*/
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {
    @Resource
    private StateMachine<OrderStatus, OrderStatusChangeEvent> orderStateMachine;
//    @Resource
//    private StateMachinePersister<OrderStatus, OrderStatusChangeEvent, String> stateMachineMemPersister;
    @Resource
    private StateMachinePersister<OrderStatus, OrderStatusChangeEvent, String> stateMachineRedisPersister;
    @Resource
    private OrderMapper orderMapper;

    /**
     * 创建订单
     *
     * @param order
     * @return
     */
    @Override
    public Order create(Order order) {
        order.setStatus(OrderStatus.WAIT_PAYMENT.getKey());
        this.save(order);
        return order;
    }

    /**
     * 对订单进行支付
     *
     * @param id
     * @return
     */
    @Override
    public Order pay(Long id) {
        Order order = orderMapper.selectById(id);
        log.info("线程名称：{},尝试支付，订单号：{}", Thread.currentThread().getName(), id);
        if (!sendEvent(OrderStatusChangeEvent.PAYED, order, CommonConstants.payTransition)) {
            log.error("线程名称：{},支付失败, 状态异常，订单信息：{}", Thread.currentThread().getName(), order);
            throw new RuntimeException("支付失败, 订单状态异常");
        }
        return order;
    }

    /**
     * 对订单进行发货
     *
     * @param id
     * @return
     */
    @Override
    public Order deliver(Long id) {
        Order order = orderMapper.selectById(id);
        log.info("线程名称：{},尝试发货，订单号：{}", Thread.currentThread().getName(), id);
        if (!sendEvent(OrderStatusChangeEvent.DELIVERY, order,CommonConstants.deliverTransition)) {
            log.error("线程名称：{},发货失败, 状态异常，订单信息：{}", Thread.currentThread().getName(), order);
            throw new RuntimeException("发货失败, 订单状态异常");
        }
        return order;
    }

    /**
     * 对订单进行确认收货
     *
     * @param id
     * @return
     */
    @Override
    public Order receive(Long id) {
        Order order = orderMapper.selectById(id);
        log.info("线程名称：{},尝试收货，订单号：{}", Thread.currentThread().getName(), id);
        if (!sendEvent(OrderStatusChangeEvent.RECEIVED, order,CommonConstants.receiveTransition)) {
            log.error("线程名称：{},收货失败, 状态异常，订单信息：{}", Thread.currentThread().getName(), order);

            throw new RuntimeException("收货失败, 订单状态异常");
        }
        return order;
    }

    /**
     * 发送订单状态转换事件
     * synchronized修饰保证这个方法是线程安全的
     *
     * @param changeEvent
     * @param order
     * @return
     */
    private synchronized boolean sendEvent(OrderStatusChangeEvent changeEvent, Order order,String key) {
        boolean result = false;
        try {
            //尝试恢复状态机状态
            stateMachineRedisPersister.restore(orderStateMachine, String.valueOf(order.getId()));
            Message message = MessageBuilder.withPayload(changeEvent).setHeader("order", order).build();
            result = orderStateMachine.sendEvent(message);

            if(!result){
                return false;
            }
            //获取到监听的结果信息
            Integer o = (Integer) orderStateMachine.getExtendedState().getVariables().get(key + order.getId());
            //操作完成之后,删除本次对应的key信息
            orderStateMachine.getExtendedState().getVariables().remove(key+order.getId());
            //如果事务执行成功，则持久化状态机
            if(Objects.equals(1,Integer.valueOf(o))){
                //持久化状态机状态
                stateMachineRedisPersister.persist(orderStateMachine, String.valueOf(order.getId()));
            }else {
                //订单执行业务异常
                return false;
            }
        } catch (Exception e) {
            log.error("订单操作失败:{}", e);
        }
        return result;
    }
}


