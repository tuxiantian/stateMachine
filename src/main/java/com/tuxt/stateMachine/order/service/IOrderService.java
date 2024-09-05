package com.tuxt.stateMachine.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tuxt.stateMachine.order.entity.Order;

/**
* @author tuxiantian
*/
public interface IOrderService extends IService<Order> {

    Order create(Order order);

    Order pay(Long id);

    Order deliver(Long id);

    Order receive(Long id);
}


