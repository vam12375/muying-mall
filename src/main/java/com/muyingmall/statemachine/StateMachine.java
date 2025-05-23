package com.muyingmall.statemachine;

/**
 * 状态机接口
 * 
 * @param <S> 状态类型
 * @param <E> 事件类型
 * @param <C> 上下文类型
 */
public interface StateMachine<S, E, C> {

    /**
     * 触发状态转换事件
     * 
     * @param currentState 当前状态
     * @param event        事件
     * @param context      上下文
     * @return 转换后的状态
     */
    S sendEvent(S currentState, E event, C context);

    /**
     * 检查状态是否可转换
     * 
     * @param currentState 当前状态
     * @param targetState  目标状态
     * @return 是否可转换
     */
    boolean canTransit(S currentState, S targetState);

    /**
     * 获取指定状态可以转换到的下一个状态集合
     * 
     * @param currentState 当前状态
     * @return 可转换的状态集合
     */
    S[] getPossibleNextStates(S currentState);
}