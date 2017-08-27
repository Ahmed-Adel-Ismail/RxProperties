package io.reactivex.properties;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Ahmed Adel Ismail on 5/10/2017.
 */
public class PropertyTest
{

    @Test
    public void filterNumbersFromSingleObservableAndAcceptEvenNumbers() throws Exception {
        Property<Integer> allNumbers = new Property<>(0);
        Property<Integer> evenNumbers = new Property<>(0);
        evenNumbers.filter(new Predicate<Integer>()
        {
            @Override
            public boolean test(@NonNull Integer integer) throws Exception {
                return integer % 2 == 0;
            }
        });

        allNumbers.asObservable().subscribe(evenNumbers);
        allNumbers.set(1);
        assertTrue(evenNumbers.get() == 0);
        allNumbers.set(2);
        assertTrue(evenNumbers.get() == 2);
        allNumbers.set(3);
        assertTrue(evenNumbers.get() == 2);
        allNumbers.set(4);
        assertTrue(evenNumbers.get() == 4);

    }

    @Test
    public void filterNumbersFromIterableObservableAndAcceptEvenNumbers() throws Exception {

        Property<List<Integer>> allNumbers = new Property<>(Arrays.asList(1, 2, 3, 4));
        Property<Integer> evenNumbers = new Property<>(0);

        evenNumbers.filter(new Predicate<Integer>()
        {
            @Override
            public boolean test(@NonNull Integer integer) throws Exception {
                return integer % 2 == 0;
            }
        }).onUpdate(new Consumer<Integer>()
        {
            @Override
            public void accept(@NonNull Integer integer) throws Exception {
                assertTrue(integer % 2 == 0);
            }
        });


        allNumbers.asObservableFromIterable(Integer.class).subscribe(evenNumbers);
        assertTrue(evenNumbers.get() == 4);

    }


    @Test
    public void asObservableWithInitialValue() throws Exception {

        final Property<Integer> result = new Property<>(0);
        Property<Integer> property = new Property<>(10);

        property.asObservable().subscribe(result);

        assertTrue(result.get().equals(10));
    }

    @Test
    public void asObservableWithLazySetValue() throws Exception {
        final Property<Integer> result = new Property<>(0);
        Property<Integer> property = new Property<>(0);
        property.asObservable().subscribe(result);
        property.set(20);
        assertTrue(result.get().equals(20));
    }

    @Test
    public void asObservableThenDisposeThenSetValueAgain() throws Exception {
        final Property<Integer> result = new Property<>(0);
        result.onConsumerAccept(new Function<Integer, Integer>()
        {
            @Override
            public Integer apply(Integer integer) {
                result.set(result.get() + integer);
                return integer;
            }
        });


        Property<Integer> property = new Property<>(0);
        Disposable d = property.asObservable().subscribe(result);


        property.set(20);
        d.dispose();
        property.set(30);


        assertTrue(result.get() == 20);
    }

    @Test
    public void asObservableForTwoSubscribersAndBothAreUpdated() throws Exception {
        final Property<Integer> resultOne = new Property<>(0);
        final Property<Integer> resultTwo = new Property<>(0);

        Property<Integer> property = new Property<>(0);
        property.asObservable().subscribe(resultOne);
        property.asObservable().subscribe(resultTwo);

        property.set(20);

        assertTrue(resultOne.get().equals(resultTwo.get()) && resultOne.get().equals(20));
    }

    @Test
    public void asIterableObservable() throws Exception {
        final BooleanProperty result = new BooleanProperty(false);
        Property<ArrayList<String>> property = new Property<>(new ArrayList<String>());
        property.get().add("A");
        property.get().add("B");
        property.get().add("C");
        property.get().add("D");

        property.asObservableFromIterable(String.class).forEach(new Consumer<String>()
        {
            @Override
            public void accept(String s) throws Exception {
                result.set(true);
            }
        });
        assertTrue(result.isTrue());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void ThrowErrorOnGetAndCrash() throws Exception {
        Property<String> property = new Property<>();
        property.onGet(throwErrorOnGettingC());
        property.set("A");
        property.set("B");
        property.set("C");
        property.get();
    }

    @Test
    public void DoClearAndInvokeOnClearConsumer() throws Exception {
        Property<String> property = new Property<>();
        final BooleanProperty result = new BooleanProperty(false);
        property.onClear(new Consumer<String>()
        {
            @Override
            public void accept(@NonNull String s) throws Exception {
                result.set(true);
            }
        });
        property.set("A");
        property.set("B");
        property.clear();

        assertTrue(result.isTrue());
    }


    @Test
    public void asObservableInvokeOnCompleteWhenCleared() throws Exception {
        final BooleanProperty result = new BooleanProperty(false);
        Property<String> property = new Property<>();
        property.asObservable()
                .subscribe(doNothingOnNext(),
                        new Consumer<Throwable>()
                        {
                            @Override
                            public void accept(@NonNull Throwable throwable) throws Exception {
                                // do nothing
                            }
                        }, new Action()
                        {
                            @Override
                            public void run() throws Exception {
                                result.set(true);
                            }
                        });

        property.set("A");
        property.set("B");
        property.set("C");
        property.clear();


        assertTrue(result.isTrue());


    }

    @Test
    public void asObservableThrowErrorOnGetAndReceiveItInOnError() throws Exception {
        Property<String> property = new Property<>();
        property.onGet(throwErrorOnGettingC());
        property.asObservable().subscribe(doNothingOnNext(), passTestOnError(), failTestOnComplete());

        property.set("A");
        property.set("B");
        property.set("C");
        property.set("D");


    }

    @Test
    public void asObserverForAnObservableThatEmitsSameType() throws Exception {
        Property<Integer> observableProperty = new Property<>();
        Property<Integer> observerProperty = new Property<>();


        observableProperty.asObservable().subscribe(observerProperty);

        observableProperty.set(10);
        assertTrue(observerProperty.get().equals(10));

    }

    @Test
    public void asObserverForAnObservableThatEmitsDifferentType() throws Exception {
        Property<Integer> observableProperty = new Property<>();
        Property<String> observerProperty = new Property<>();


        observableProperty.asObservable().subscribe(observerProperty.asConsumer(
                new Function<Integer, String>()
                {
                    @Override
                    public String apply(@NonNull Integer integer) throws Exception {
                        return String.valueOf(integer);
                    }
                }));

        observableProperty.set(10);
        assertTrue(observerProperty.get().equals("10"));

    }

    private Function<String, String> throwErrorOnGettingC() {
        return new Function<String, String>()
        {
            @Override
            public String apply(@NonNull String s) throws Exception {
                if ("C".equals(s)) {
                    throw new UnsupportedOperationException();
                }
                return s;
            }
        };
    }

    private Consumer<String> doNothingOnNext() {
        return new Consumer<String>()
        {
            @Override
            public void accept(@NonNull String s) throws Exception {
                if ("D".equals(s)) {
                    assertTrue(false);
                }
            }
        };
    }


    private Consumer<Throwable> passTestOnError() {
        return new Consumer<Throwable>()
        {
            @Override
            public void accept(@NonNull Throwable throwable) throws Exception {
                assertTrue(true);
            }
        };
    }

    private Action failTestOnComplete() {
        return new Action()
        {
            @Override
            public void run() throws Exception {
                assertTrue(false);
            }
        };
    }

    @Test
    public void map() throws Exception {
        Property<Integer> p = new Property<>(10);
        String s = p.map(new Function<Integer, String>()
        {
            @Override
            public String apply(@NonNull Integer integer) throws Exception {
                return String.valueOf(integer);
            }
        });
        assertTrue(s.equals("10"));
    }

    @Test
    public void flatMap() throws Exception {
        Property<Integer> p = new Property<>(10);
        Property<String> s = p.flatMap(new Function<Integer, Property<String>>()
        {
            @Override
            public Property<String> apply(@NonNull Integer integer) throws Exception {
                return new Property<>(String.valueOf(integer));
            }
        });

        assertTrue(s.get().equals("10"));
    }

    @Test
    public void asMaybeWithValue_emitValue() throws Exception {
        Property<Boolean> result = new Property<>(false);
        Property<Integer> p = new Property<>(10);
        p.asMaybe().subscribe(result.asConsumer(new Function<Integer, Boolean>()
        {
            @Override
            public Boolean apply(@NonNull Integer integer) throws Exception {
                return true;
            }
        }));
        assertTrue(result.get());
    }

    @Test
    public void asMaybeWithNoValue_DoNotEmitValue() throws Exception {
        Property<Boolean> result = new Property<>(true);
        Property<Integer> p = new Property<>(null);
        p.asMaybe().subscribe(result.asConsumer(new Function<Integer, Boolean>()
        {
            @Override
            public Boolean apply(@NonNull Integer integer) throws Exception {
                return false;
            }
        }));
        assertTrue(result.get());
    }

    @Test
    public void asMaybePropertyWithValue_emitValue() throws Exception {
        Property<Boolean> result = new Property<>(false);
        Property<Integer> p = new Property<>(10);
        p.asMaybeProperty().subscribe(result.asConsumer(new Function<Property<Integer>, Boolean>()
        {
            @Override
            public Boolean apply(@NonNull Property<Integer> integer) throws Exception {
                return true;
            }
        }));
        assertTrue(result.get());
    }

    @Test
    public void asMaybePropertyWithNoValue_DoNotEmitValue() throws Exception {
        Property<Boolean> result = new Property<>(true);
        Property<Integer> p = new Property<>(null);
        p.asMaybeProperty().subscribe(result.asConsumer(new Function<Property<Integer>, Boolean>()
        {
            @Override
            public Boolean apply(@NonNull Property<Integer> integer) throws Exception {
                return false;
            }
        }));
        assertTrue(result.get());
    }

    @Test
    public void updateTypeOnInitializeWithNonNull() throws Exception {
        Property<Boolean> property = new Property<>(true);
        assertTrue(property.getType().blockingGet().equals(Boolean.class));
    }

    @Test
    public void updateTypeOnSetWithNonNull() throws Exception {
        Property<Integer> property = new Property<>();
        property.set(10);
        assertTrue(property.getType().blockingGet().equals(Integer.class));
    }

    @Test
    public void doNotUpdateTypeOnSetWithNull() throws Exception {
        Property<Integer> property = new Property<>();
        property.set(null);
        assertNull(property.getType().blockingGet());
    }

}