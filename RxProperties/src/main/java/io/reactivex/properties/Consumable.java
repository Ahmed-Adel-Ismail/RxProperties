package io.reactivex.properties;


import io.reactivex.properties.exceptions.ConsumedException;

import io.reactivex.Maybe;

/**
 * a Class that holds an Object that is used only once, you will need to invoke {@link #set(Object)}
 * to set an object, and as soon as {@link #get()} or {@link #consume()} is invoked,
 * the reference to this Object is set to {@code null}, you can invoke {@link #set(Object)}
 * again and so on
 * <p>
 * Created by Ahmed Adel on 12/28/2016.
 */
public class Consumable<T> extends Property<T>
{

    public Consumable() {
    }

    public Consumable(T object) {
        super(object);
    }

    /**
     * set an consumable Object that it's reference will be cleared as soon as a call to
     * {@link #consume()} or {@link #get()} is invoked
     *
     * @param object the object to be consumed later on
     */
    @Override
    public T set(T object) {
        super.set(object);
        return object;
    }

    /**
     * get the consumable Object if exists, if it is already consumed, this method will return
     * {@code null}
     *
     * @return the consumable object, or {@code null}
     */
    @Override
    public T get() {
        T newObject = super.get();
        object = null;
        return newObject;
    }

    /**
     * get the consumable Object if exists, if it is already consumed, this method will throw
     * a {@link ConsumedException}
     *
     * @return the consumable object
     * @throws ConsumedException if the consumable object is {@code null}
     */
    public T consume() throws ConsumedException {
        if (object != null) {
            return get();
        }
        else {
            throw new ConsumedException();
        }
    }

    /**
     * if this {@link Consumable} contains a value, it will be emitted by this {@link Maybe} and
     * will be reset to {@code null} ... same as invoking {@link #get()}, if there was no value
     * stored, the {@link Maybe#empty()} will be invoked, causing nothing to be emitted
     */
    @Override
    public Maybe<T> asMaybe() {
        return super.asMaybe();
    }
}
