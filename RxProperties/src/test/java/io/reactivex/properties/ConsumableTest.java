package io.reactivex.properties;


import junit.framework.Assert;

import org.junit.Test;

import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.properties.exceptions.ConsumedException;

/**
 * Created by Ahmed Adel Ismail on 5/13/2017.
 */
public class ConsumableTest
{

    @Test
    public void addOneValueAndTryToGetItOnceAndReturnValue() throws Exception {
        Consumable<String> consumable = new Consumable<>();
        consumable.set("hello");
        Assert.assertNotNull(consumable.get());

    }

    @Test(expected = ConsumedException.class)
    public void addOneValueAndTryToGetItTwiceAndThrowExceptionInTheSecondTime() throws Exception {
        Consumable<String> consumable = new Consumable<>();
        consumable.set("hello");
        consumable.get();
        consumable.consume();

    }

    @Test(expected = ConsumedException.class)
    public void addOneValueAndTryToConsumeItTwiceAndThrowExceptionInTheSecondTime() throws Exception {
        Consumable<String> consumable = new Consumable<>("hello");
        consumable.consume();
        consumable.consume();

    }

    @Test
    public void addOneValueAndTryToGetItTwiceThenTheSecondTimeIsNull() throws Exception {
        Consumable<String> consumable = new Consumable<>();
        consumable.set("hello");
        consumable.get();
        Assert.assertNull(consumable.get());

    }

    @Test
    public void addOneValueAndConsumeIt_TheValueIsResetToNull() {
        Consumable<String> consumable = new Consumable<>();
        consumable.set("hello");
        consumable.consume();
        Assert.assertNull(consumable.get());
    }

    @Test
    public void addOneValueAndConsumeItWithAsMaybe_TheValueIsResetToNull() {
        Consumable<String> consumable = new Consumable<>();
        consumable.set("hello");
        consumable.asMaybe().subscribe(new Consumer<String>()
        {
            @Override
            public void accept(@NonNull String s) throws Exception {

            }
        });
        Assert.assertNull(consumable.get());
    }
}