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
     * 发送事件，触发状态转换
     * 
     * @param currentState 当前状态
     * @param event        事件
     * @param context      上下文
     * @return 转换后的状态
     */
    S sendEvent(S currentState, E event, C context);

    /**
     * 检查是否可以从当前状态转换到目标状态
     * 
     * @param currentState 当前状态
     * @param targetState  目标状态
     * @return 是否可以转换
     */
    boolean canTransit(S currentState, S targetState);

    /**
     * 获取所有可能的下一个状态
     * 
     * @param currentState 当前状态
     * @return 可能的下一个状态数组
     */
    S[] getPossibleNextStates(S currentState);
}