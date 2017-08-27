package io.reactivex.properties;


import java.util.Collection;
import java.util.concurrent.Callable;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.properties.exceptions.InvocationException;
import io.reactivex.properties.exceptions.RuntimeExceptionConverter;

/**
 * a class that acts as a property, it holds it's {@link #set(Object)} and {@link #get()}
 * methods for the stored value
 * <p>
 * Created by Ahmed Adel on 12/28/2016.
 */
public class Property<T> implements
        Callable<T>,
        Consumer<T>,
        Clearable,
        Emptyable
{


    T object;
    private Class<?> type;
    private Predicate<T> filter;
    private BiFunction<T, T, T> onSet;
    private Function<T, T> onGet;
    private Consumer<T> onUpdate;
    private Consumer<T> onClear;
    private Function<T, T> onConsumerAccept;
    private final EmittersGroup<T> emitters = new EmittersGroup<>();


    public Property() {
    }

    public Property(T object) {
        this.object = object;
        if (object != null) {
            type = object.getClass();
        }
    }

    /**
     * set an Object as the value of this property
     *
     * @param object the object to be stored
     * @return the stored object after being updated
     */
    public T set(T object) {

        try {
            doSet(object);
        } catch (Throwable e) {
            throw new InvocationException("failed to execute set(" + object + ")", e);
        }

        if (onUpdate != null) {
            doUpdate(object);
        }

        if (!emitters.isEmpty()) {
            notifyEmittersWithValueSet(object);
        }

        return object;
    }


    private void doSet(T object) throws Exception {
        if (filter != null) {
            if (filter.test(object)) {
                setValue(object);
            }
        } else {
            setValue(object);
        }
    }

    private void setValue(T object) throws Exception {

        if (onSet != null) {
            this.object = onSet.apply(this.object, object);
        } else {
            this.object = object;
        }

        if (this.object != null) {
            type = this.object.getClass();
        }
    }

    private void doUpdate(T object) {
        try {
            onUpdate.accept(this.object);
        } catch (Throwable e) {
            throw new InvocationException("failed to execute onUpdate() inside filter("
                    + object + ") ", e);
        }
    }

    private void notifyEmittersWithValueSet(T object) {
        if (object != null) {
            try {
                emitters.onNext(get());
            } catch (Throwable e) {
                emitters.onError(e);
            }
        } else {
            emitters.onError(new NullPointerException("value set to null"));
        }
    }


    /**
     * get the Object referenced as the value of this property
     *
     * @return the value if stored, or {@code null} if nothing is stored
     */
    public T get() {
        if (onGet != null) {
            return invokeOnGet();
        } else {
            return object;
        }
    }

    private T invokeOnGet() {
        try {
            return onGet.apply(object);
        } catch (Throwable e) {
            throw new RuntimeExceptionConverter().apply(e);
        }
    }

    /**
     * an implementation of the {@link Callable} interface, where calling this method will
     * invoke {@link #get()}, you can use the {@link Function} passed to {@link #onGet(Function)}
     * to do any operation
     *
     * @return the result of invoking {@link #get()}
     */
    @Override
    public T call() {
        return get();
    }

    /**
     * set a {@link Function} that will be executed when {@link #get()} method is invoked,
     * notice that this method may receive {@code null} if no current value is set in this
     * {@link Property}
     *
     * @param onGet the {@link Function} that will be executed every time {@link #get()} method is
     *              invoked, it will take the original value stored as a parameter, and it will
     *              return the updated value as it's return value (which will then be returned
     *              by the {@link #get()} method)
     * @param <S>   the sub-class of this {@link Property}
     * @return the sub-class of this {@link Property} to be used for chaining
     */
    @SuppressWarnings("unchecked")
    public <S extends Property<T>> S onGet(Function<T, T> onGet) {
        this.onGet = onGet;
        return (S) this;
    }

    /**
     * set a {@link Predicate} that will be executed when {@link #set(Object)} method is invoked to
     * filter it, it wont update the current value if it returned {@code false}
     *
     * @param filter the {@link Predicate} that will be executed every time {@link #set(Object)}
     *               method is invoked, it will take the original value passed as a parameter, and it
     *               will return {@code true} if the value is accepted, or {@code false} if the
     *               value is not
     * @return the sub-class of this {@link Property} to be used for chaining
     */
    @SuppressWarnings("unchecked")
    public <S extends Property<T>> S filter(Predicate<T> filter) {
        this.filter = filter;
        return (S) this;
    }

    /**
     * set a {@link BiFunction} that will be executed when {@link #set(Object)} method is invoked,
     * notice that this method will receive the old value as the first parameter, the
     * new value as the second parameter, and will return the final value
     * <p>
     * this method is invoked if the set {@link #filter(Predicate)} returned {@code true}, or
     * if {@link #filter(Predicate)} was not set
     *
     * @param onSet the {@link Function} that will be executed every time {@link #get()} method is
     *              invoked, it will take the original value stored as a parameter, and it will
     *              return the updated value as it's return value (which will then be returned
     *              by the {@link #get()} method)
     * @param <S>   the sub-class of this {@link Property}
     * @return the sub-class of this {@link Property} to be used for chaining
     */
    @SuppressWarnings("unchecked")
    public <S extends Property<T>> S onSet(BiFunction<T, T, T> onSet) {
        this.onSet = onSet;
        return (S) this;
    }

    /**
     * set a {@link Consumer} that will be executed when {@link #set(Object)} method finishes it's
     * invocation and the value is updated
     *
     * @param onUpdate the {@link Consumer} that will be executed every time {@link #set(Object)}
     *                 method is invoked and finished, it will take the final value updated in this
     *                 instance, notice that this is invoked after the value is updated
     * @param <S>      the sub-class of this {@link Property}
     * @return the sub-class of this {@link Property} to be used for chaining
     */
    @SuppressWarnings("unchecked")
    public <S extends Property<T>> S onUpdate(Consumer<T> onUpdate) {
        this.onUpdate = onUpdate;
        return (S) this;
    }

    /**
     * set a {@link Consumer} that will be executed when {@link #clear()} method is invoked
     *
     * @param onClear the {@link Consumer} that will be executed
     * @param <S>     the sub-class of this {@link Property}
     * @return the sub-class of this {@link Property} to be used for chaining
     */
    @SuppressWarnings("unchecked")
    public <S extends Property<T>> S onClear(Consumer<T> onClear) {
        this.onClear = onClear;
        return (S) this;
    }


    /**
     * set an optional command that will be executed on the object passed to {@link #accept(Object)}
     * method, usually this is to modify / validate the objects received from Observables to this
     * {@link Property} when it is acting as a subscriber
     *
     * @param onConsumerAccept the {@link Function} that will have it's returned value as the new
     *                         value passed to {@link #set(Object)}
     * @param <S>              the sub-class of this {@link Property}
     * @return the sub-class of this {@link Property} to be used for chaining
     */
    @SuppressWarnings("unchecked")
    public <S extends Property<T>> S onConsumerAccept(Function<T, T> onConsumerAccept) {
        this.onConsumerAccept = onConsumerAccept;
        return (S) this;
    }


    /**
     * the default implementation for {@link Consumer} interface, which executes
     * {@link #set(Object)} method as soon as it is invoked, if {@link #onConsumerAccept(Function)}
     * has set a {@link Function}, it will be executed on the object passed, and it's
     * returned value will be the object to be passed to {@link #set(Object)} method
     *
     * @param object the object receieved from the Observer
     */
    @Override
    public void accept(T object) {
        if (onConsumerAccept != null) {
            invokeOnConsumerAccept(object);
        } else {
            set(object);
        }
    }

    private void invokeOnConsumerAccept(T object) {
        try {
            set(onConsumerAccept.apply(object));
        } catch (Throwable e) {
            throw new InvocationException("failed to execute accept(" + object + ")", e);
        }
    }

    @Override
    public boolean isEmpty() {
        return object == null;
    }


    @Override
    public void clear() {
        if (onClear != null) {
            invokeOnClear();
        }
        object = null;
        filter = null;
        onGet = null;
        onUpdate = null;
        emitters.onComplete();
        emitters.clear();
    }

    private void invokeOnClear() {
        try {
            onClear.accept(object);
        } catch (Throwable e) {
            throw new RuntimeExceptionConverter().apply(e);
        }
        onClear = null;
    }


    /**
     * creates an {@link Observable} from this {@link Property}
     *
     * @return an {@link Observable} that emits the value of this {@link Property}, if
     * this {@link Property} contains an {@link Iterable}, you can use
     * {@link #asObservableFromIterable(Class)} instead
     */
    public Observable<T> asObservable() {
        return Observable.create(new ObservableOnSubscribe<T>()
        {
            @Override
            public void subscribe(@NonNull ObservableEmitter<T> e) throws Exception {
                if (!emitters.contains(e)) {
                    updateEmittersAndInvokeOnNextIfNotNull(e);
                }
            }
        });
    }

    private void updateEmittersAndInvokeOnNextIfNotNull(ObservableEmitter<T> e) {
        emitters.update(e);
        if (object != null) {
            e.onNext(get());
        }
    }

    /**
     * creates an {@link Observable} from this {@link Property} value, which should be
     * a {@link Iterable}, like {@link Collection} classes for example
     *
     * @param iterableItemType the type of the stored items in the {@link Iterable} value
     *                         of this {@link Property}
     * @return an {@link Observable} to be used
     * @throws UnsupportedOperationException if the stored value is {@code null}, or if
     *                                       the values inside that iterable does not match
     *                                       the type passed in the parameter, or if the
     *                                       creation of the {@link Observable} failed
     */
    @SuppressWarnings("unchecked warning")
    public <V> Observable<V> asObservableFromIterable(Class<V> iterableItemType) {
        if (object == null || !(object instanceof Iterable)) {
            throw new UnsupportedOperationException("no Iterable to use as Observable source");
        }

        try {
            Iterable<V> observableSource = (Iterable<V>) object;
            return Observable.fromIterable(observableSource);
        } catch (ClassCastException e) {
            throw new UnsupportedOperationException("stored value is not a Collection of the passed type");
        } catch (Exception e) {
            throw new UnsupportedOperationException("failed to create : " + e.getMessage());
        }
    }

    /**
     * map the value stored in this property to another value
     *
     * @param mapper the {@link Function} that will do the mapping
     * @param <R>    the expected return type
     * @return a mapped item
     * @throws NullPointerException if the current value is {@code null}
     */
    public <R> R map(Function<T, R> mapper) throws NullPointerException {
        if (object == null) {
            throw new NullPointerException("null value");
        }

        try {
            return mapper.apply(object);
        } catch (Throwable e) {
            throw new RuntimeExceptionConverter().apply(e);
        }
    }

    /**
     * map the value stored in this property to another {@link Property}
     *
     * @param mapper the {@link Function} that will do the mapping
     * @param <R>    the expected return type
     * @return a mapped item
     * @throws NullPointerException if the current value is {@code null}
     */
    public <P extends Property<R>, R> P flatMap(Function<T, P> mapper) throws NullPointerException {
        if (object == null) {
            throw new NullPointerException("null value");
        }

        try {
            return mapper.apply(object);
        } catch (Throwable e) {
            throw new RuntimeExceptionConverter().apply(e);
        }
    }


    /**
     * get the current value in this {@link Property} in a {@link Maybe}, this method invokes the
     * {@link #get()} method
     *
     * @return if {@link #get()} returned {@code null},
     * the {@link Maybe} will be empty, else it will return the value stored in this
     * {@link Property} with it's {@code non-null} value
     */
    public Maybe<T> asMaybe() {
        if (object != null) {
            return Maybe.just(get());
        } else {
            return Maybe.empty();
        }
    }

    /**
     * get the current {@link Property} in a {@link Maybe} if the value in this {@link Property}
     * is not {@code null}, this method does not invoke the {@link #get()} method
     *
     * @return if value stored in this {@link Property} is {@code null}
     * the {@link Maybe} will be empty, else it will a {@link Maybe} of this
     * {@link Property} with it's {@code non-null} value
     */
    public Maybe<Property<T>> asMaybeProperty() {
        if (object != null) {
            return Maybe.just(this);
        } else {
            return Maybe.empty();
        }
    }

    /**
     * create a {@link Consumer} that invokes {@link #set(Object)} every time it is updated
     *
     * @param mapper the {@link Function} that converts the passed value to the type that
     *               can be passed to {@link #set(Object)} method
     * @param <V>    the type that will be passed to the {@link Consumer}
     * @return a {@link Consumer} that uses the mapper {@link Function} to convert any passed
     * object to another object that will be passed to the {@link #set(Object)} immediatly
     */
    public <V> Consumer<V> asConsumer(final Function<V, T> mapper) {
        return new Consumer<V>()
        {
            @Override
            public void accept(@NonNull V v) throws Exception {
                set(mapper.apply(v));
            }
        };
    }

    /**
     * get the {@link Class} type of the object stored in this {@link Property}
     * @return a {@link Maybe} holding the {@link Class} type of the value in this {@link Property}
     * if available, or empty if not set yet
     */
    Maybe<? extends Class<?>> getType() {
        if (type != null) {
            return Maybe.just(type);
        } else {
            return Maybe.empty();
        }
    }

}
