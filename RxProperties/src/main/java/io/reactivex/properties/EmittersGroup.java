package io.reactivex.properties;


import java.lang.ref.WeakReference;
import java.util.LinkedList;

import io.reactivex.Emitter;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * a group of {@link Emitter} instances
 * <p>
 * Created by Ahmed Adel Ismail on 4/24/2017.
 */
class EmittersGroup<T> extends LinkedList<WeakReference<ObservableEmitter<T>>>
        implements
        Emitter<T>
{


    public void update(ObservableEmitter<T> object) {

        try {
            clearDisposedEmitters();
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            add(new WeakReference<>(object));
        }
    }

    private void clearDisposedEmitters() throws NullPointerException {
        Observable.fromIterable(this)
                .filter(byNonNullReference())
                .filter(byDisposed())
                .blockingForEach(invokeRemove());
    }

    private Predicate<WeakReference<ObservableEmitter<T>>> byNonNullReference() {
        return new Predicate<WeakReference<ObservableEmitter<T>>>()
        {
            @Override
            public boolean test(@NonNull WeakReference<ObservableEmitter<T>> ref) throws Exception {
                return ref.get() != null;
            }
        };
    }

    private Predicate<WeakReference<ObservableEmitter<T>>> byDisposed() {
        return new Predicate<WeakReference<ObservableEmitter<T>>>()
        {
            @Override
            public boolean test(@NonNull WeakReference<ObservableEmitter<T>> ref) throws Exception {
                return ref.get().isDisposed();
            }
        };
    }

    private Consumer<WeakReference<ObservableEmitter<T>>> invokeRemove() {
        return new Consumer<WeakReference<ObservableEmitter<T>>>()
        {
            @Override
            public void accept(WeakReference<ObservableEmitter<T>> ref) throws Exception {
                remove(ref);
            }
        };
    }

    public final boolean contains(final ObservableEmitter<T> emitter) {
        return Observable.fromIterable(this)
                .map(toObservableEmitter())
                .filter(byNonNull())
                .map(toEmitterEquals(emitter))
                .any(isTrue())
                .onErrorReturn(toFalse())
                .blockingGet();
    }

    private Function<WeakReference<ObservableEmitter<T>>, ObservableEmitter<T>> toObservableEmitter() {
        return new Function<WeakReference<ObservableEmitter<T>>, ObservableEmitter<T>>()
        {
            @Override
            public ObservableEmitter<T> apply(@NonNull WeakReference<ObservableEmitter<T>> ref) throws Exception {
                return ref.get();
            }
        };
    }

    private Predicate<ObservableEmitter<T>> byNonNull() {
        return new Predicate<ObservableEmitter<T>>()
        {
            @Override
            public boolean test(@NonNull ObservableEmitter<T> emitter) throws Exception {
                return emitter != null;
            }
        };
    }

    private Function<ObservableEmitter<T>, Boolean> toEmitterEquals(final ObservableEmitter<T> emitter) {
        return new Function<ObservableEmitter<T>, Boolean>()
        {
            @Override
            public Boolean apply(@NonNull ObservableEmitter<T> otherEmitter) throws Exception {
                return emitter.equals(otherEmitter);
            }
        };
    }

    private Predicate<Boolean> isTrue() {
        return new Predicate<Boolean>()
        {
            @Override
            public boolean test(@NonNull Boolean booleanValue) throws Exception {
                return booleanValue;
            }
        };
    }

    private Function<Throwable, Boolean> toFalse() {
        return new Function<Throwable, Boolean>()
        {
            @Override
            public Boolean apply(@NonNull Throwable throwable) throws Exception {
                return false;
            }
        };
    }


    @Override
    public void onNext(final T value) {
        Observable.fromIterable(this).forEach(new Consumer<WeakReference<ObservableEmitter<T>>>()
        {
            @Override
            public void accept(WeakReference<ObservableEmitter<T>> e) throws Exception {
                ObservableEmitter<T> emitter = e.get();
                if (emitter != null && !emitter.isDisposed()) {
                    emitter.onNext(value);
                }
            }
        });
    }

    @Override
    public void onError(final Throwable error) {
        Observable.fromIterable(this).forEach(new Consumer<WeakReference<ObservableEmitter<T>>>()
        {
            @Override
            public void accept(WeakReference<ObservableEmitter<T>> e) throws Exception {
                ObservableEmitter<T> emitter = e.get();
                if (emitter != null && !emitter.isDisposed()) {
                    emitter.onError(error);
                }
            }
        });
    }

    @Override
    public void onComplete() {
        Observable.fromIterable(this).forEach(new Consumer<WeakReference<ObservableEmitter<T>>>()
        {
            @Override
            public void accept(WeakReference<ObservableEmitter<T>> e) throws Exception {
                ObservableEmitter<T> emitter = e.get();
                if (emitter != null && !emitter.isDisposed()) {
                    emitter.onComplete();
                }
            }
        });
    }
}
