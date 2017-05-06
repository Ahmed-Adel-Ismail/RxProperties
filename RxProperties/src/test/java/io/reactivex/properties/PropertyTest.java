package io.reactivex.properties;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

import static org.junit.Assert.assertTrue;

/**
 * a test class for {@link Property}
 * <p>
 * Created by Ahmed Adel Ismail on 5/6/2017.
 */
public class PropertyTest {


    @Test
    public void filterNumbersFromSingleObservableAndAcceptEvenNumbers() throws Exception {
        Property<Integer> allNumbers = new Property<>(0);
        Property<Integer> evenNumbers = new Property<>(0);
        evenNumbers.filter(new Predicate<Integer>() {
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

        evenNumbers.filter(new Predicate<Integer>() {
            @Override
            public boolean test(@NonNull Integer integer) throws Exception {
                return integer % 2 == 0;
            }
        }).onUpdate(new Consumer<Integer>() {
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
        result.onAccept(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer integer) {
                System.out.println(integer);
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

        property.asObservableFromIterable(String.class).forEach(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                System.out.println(s);
                result.set(true);
            }
        });
        assertTrue(result.isTrue());
    }


}