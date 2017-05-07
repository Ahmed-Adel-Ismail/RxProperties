# RxProperties
a library that provides the "Property" concept to Java, in a reactive manner, for every Property, it has it's own set() and get() method, all you have to do is to declare the variable as 

    public final Property<MyObject> myObjectProperty = new Property(new MyObject());
    
then you can use this property as follows :

    myObjectProperty.set(new MyObject());
    MyObject myObject = myObjectProperty.get()

no need for setter or getter methods

# Advanced Usage for Properties

you can add your own code inside the set() and get() methods through methods like filter(), onUpdate(), and onGet()

an example is like this :

    Property<Integer> evenNumbers = new Property<>(0);
    evenNumbers.filter(new Predicate<Integer>() {
        @Override
        public boolean test(@NonNull Integer integer) throws Exception {
            return integer % 2 == 0;
        }
    })


# RxJava2 Observable & Observer 

Properties act as an Observable and / or Observer, an example is as follows :

    Property<Integer> observable = new Property<>(10);
    final Property<Integer> observer = new Property<>(0);    
    observable.asObservable().subscribe(observer);
    
    
# More Use case examples from the PropertyTest.java

    
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

