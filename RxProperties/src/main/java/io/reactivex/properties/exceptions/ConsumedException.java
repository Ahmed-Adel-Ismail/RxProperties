package io.reactivex.properties.exceptions;


import io.reactivex.properties.Consumable;

/**
 * a {@link RuntimeException} that is thrown when attempting to invoke {@link Consumable#get()}
 * while the object was already consumed
 * <p>
 * Created by Ahmed Adel on 12/28/2016.
 */
public class ConsumedException extends RuntimeException
{

}
