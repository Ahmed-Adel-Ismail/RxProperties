package io.reactivex.properties;

import java.util.Collection;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.properties.exceptions.InvocationException;

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
        Emptyable {


    T object;
    private Predicate<T> filter;
    private Function<T, T> onGet;
    private Consumer<T> onUpdate;
    private Function<T, T> onAccept;
    private final EmittersGroup<T> emitters = new EmittersGroup<>();


    public Property(T object) {
        this.object = object;
    }

    /**
     * set an Object as the value of this property
     *
     * @param object the object to be stored
     * @return the stored object after being updated
     */
    public T set(T object) {

        if (filter != null) {
            try {
                if (filter.test(object)) this.object = object;
            } catch (Throwable e) {
                throw new InvocationException("failed to execute set(" + object + ")", e);
            }
        } else {
            this.object = object;
        }

        if (onUpdate != null) {
            try {
                onUpdate.accept(this.object);
            } catch (Throwable e) {
                throw new InvocationException("failed to execute onUpdate() inside filter("
                        + object + ") ", e);
            }

        }

        notifyEmittersWithValueSet(object);

        return object;
    }

    protected final void notifyEmittersWithValueSet(T object) {
        if (object != null && !emitters.isEmpty()) {
            emitters.onNext(get());
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
            try {
                return onGet.apply(object);
            } catch (Throwable e) {
                throw new InvocationException("failed to execute get()", e);
            }
        } else {
            return object;
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
     * set an optional command that will be executed on the object passed to {@link #accept(Object)}
     * method, usually this is to modify / validate the objects received from Observables to this
     * {@link Property} when it is acting as a subscriber
     *
     * @param onAccept the {@link Function} that will have it's returned value as the new
     *                 value passed to {@link #set(Object)}
     * @param <S>      the sub-class of this {@link Property}
     * @return the sub-class of this {@link Property} to be used for chaining
     */
    @SuppressWarnings("unchecked")
    public <S extends Property<T>> S onAccept(Function<T, T> onAccept) {
        this.onAccept = onAccept;
        return (S) this;
    }


    /**
     * the default implementation for {@link Consumer} interface, which executes
     * {@link #set(Object)} method as soon as it is invoked, if {@link #onAccept(Function)}
     * has set a {@link Function}, it will be executed on the object passed, and it's
     * returned value will be the object to be passed to {@link #set(Object)} method
     *
     * @param object the object receieved from the Observer
     */
    @Override
    public void accept(T object) {
        if (onAccept != null) {
            try {
                set(onAccept.apply(object));
            } catch (Throwable e) {
                throw new InvocationException("failed to execute accept(" + object + ")", e);
            }
        } else {
            set(object);
        }
    }

    @Override
    public boolean isEmpty() {
        return object == null;
    }

    @Override
    public void clear() {
        object = null;
        filter = null;
        onGet = null;
        onUpdate = null;
        emitters.onComplete();
        emitters.clear();
    }


    /**
     * creates an {@link Observable} from this {@link Property}
     *
     * @return an {@link Observable} that emits the value of this {@link Property}, if
     * this {@link Property} contains an {@link Iterable}, you can use
     * {@link #asObservableFromIterable(Class)} instead
     */
    public Observable<T> asObservable() {
        return Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<T> e) throws Exception {
                emitters.add(e);
                if (object != null) {
                    emitters.onNext(get());
                }
            }

        });
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


}
