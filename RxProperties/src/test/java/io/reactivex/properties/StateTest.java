package io.reactivex.properties;


import org.junit.Test;

import io.reactivex.properties.exceptions.StateIsMovingToNullException;
import io.reactivex.properties.exceptions.StateNotValidToMoveException;

import static org.junit.Assert.assertTrue;

/**
 * Created by Ahmed Adel Ismail on 5/13/2017.
 */
public class StateTest
{


    @Test
    public void moveToNextState() throws Exception {
        State<InitialState> state = new State<>(new InitialState());
        state.get().move = true;
        state.next();
        assertTrue(state.get().currentState.equals(StateOne.class));

    }

    @Test(expected = StateNotValidToMoveException.class)
    public void moveToNextButThrowNotValidToMoveException() throws Exception {
        State<InitialState> state = new State<>(new InitialState());
        state.next();
    }

    @Test(expected = StateIsMovingToNullException.class)
    public void moveToNextButThrowIsMovingToNullException() throws Exception {
        State<InitialState> state = new State<InitialState>(new StateTwo());
        state.get().move = true;
        state.next();
    }

    @Test
    public void moveToBackState() throws Exception {
        State<InitialState> state = new State<InitialState>(new StateOne());
        state.get().move = true;
        state.back();
        assertTrue(state.get().currentState.equals(InitialState.class));

    }

    @Test(expected = StateNotValidToMoveException.class)
    public void moveToBackButThrowNotValidToMoveException() throws Exception {
        State<InitialState> state = new State<InitialState>(new StateOne());
        state.back();
    }


    @Test(expected = StateIsMovingToNullException.class)
    public void moveToBackButThrowIsMovingToNullException() throws Exception {
        State<InitialState> state = new State<>(new InitialState());
        state.get().move = true;
        state.back();
    }


    static class InitialState implements SwitchableState<InitialState>
    {

        boolean move;
        Class<?> currentState;

        InitialState() {
            currentState = getClass();
        }

        @Override
        public InitialState next() throws StateIsMovingToNullException, StateNotValidToMoveException {
            if (move) {
                return new StateOne();
            }
            else {
                throw new StateNotValidToMoveException();
            }
        }

        @Override
        public InitialState back() throws StateIsMovingToNullException, StateNotValidToMoveException {
            throw new StateIsMovingToNullException();
        }
    }


    static class StateOne extends InitialState
    {
        @Override
        public InitialState next() throws StateIsMovingToNullException, StateNotValidToMoveException {
            if (move) {
                return new StateTwo();
            }
            else {
                throw new StateNotValidToMoveException();
            }
        }

        @Override
        public InitialState back() throws StateIsMovingToNullException, StateNotValidToMoveException {
            if (move) {
                return new InitialState();
            }
            else {
                throw new StateNotValidToMoveException();
            }
        }
    }

    static class StateTwo extends InitialState
    {
        @Override
        public InitialState next() throws StateIsMovingToNullException, StateNotValidToMoveException {
            throw new StateIsMovingToNullException();
        }

        @Override
        public InitialState back() throws StateIsMovingToNullException, StateNotValidToMoveException {
            if (move) {
                return new StateOne();
            }
            else {
                throw new StateNotValidToMoveException();
            }
        }
    }


}