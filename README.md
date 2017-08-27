# RxProperties
a library that provides the "Property" concept to Java, in a reactive manner, for every Property, it has it's own set() and get() method, all you have to do is to declare the variable as 

    public final Property<MyObject> myObjectProperty = new Property<>(new MyObject());
    
then you can use this property as follows :

    myObjectProperty.set(new MyObject());
    MyObject myObject = myObjectProperty.get()

no need for setter or getter methods

# Properties Types

    - Property          :   the base class of the different variations of Property
    - Consumable        :   a Property that when it's get() method is invoked, it returns it's value to null, this helps replacing the 
                            flags that we keep setting to true and false in the life cycle events
    - BooleanProperty   :   a property that holds boolean value, this property has a default value as "false", should not be used when
                            dealing with the Property as an Observable or Maybe, since it will emit a "false" when you subscribe to it
    - State             :   a Property that implements the State-Pattern, it holds an Object that implements SwitchableState interface, 
                            an interface that has 2 methods, next() and back(), where every value knows it's next state and it's 
                            previous state ... this can be useful when switching data with ViewPagers and every value is mapped to a 
                            fragment for example

# Advanced Usage for Properties

you can add your own code through functions that will be executed at later when there action happens

these are the methods from the Property.java :

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
	

an example for a property that accepts only even numbers :

    Property<Integer> evenNumbers = new Property<>(0);
    evenNumbers.filter(new Predicate<Integer>() {
        @Override
        public boolean test(@NonNull Integer integer) throws Exception {
            return integer % 2 == 0;
        }
    })

# RxJava2 related functions :

Properties already implements the Consumer interface, so they can be used in RxJava's onNext() or onError(), and you set a function that will be executed every time the accept() method is invoked through "onConsumerAccept()" method :

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
	
other functions are :
	
	map() : converts Property into another value
	
	flatMap() : converts Property into another Property
	
	asObservable() : creates an Observable that emits an item when ever the Property is updated
	
	asObservableFromIterable() : if the Property holds a List of items, this method returns an Observable that iterates over the items and emits them
	
	asMaybe() : creates a Maybe that emits an item if the Property holds any, or it will be empty if the property does not hold any value
	
	asMaybeProperty() : creates a Maybe that will emit the current property if it's value is not null, else it will be an empty Maybe
	
	asConsumer() : creates a Consumer with a mapper function, where this function converts the emitted item into the type of the property, so the Property can observe on an Observable of a different type, and every time this Observable emits an item, the mapper function converts the emitted item to the type of the observer property

an exmaple of asObservable will be : 

    Property<Integer> observable = new Property<>(10);
    Property<Integer> observer = new Property<>(0);    
    observable.asObservable().subscribe(observer);
       
# The power of MVVM pattern

Properties makes it very easy to implement the MVVM pattern, specially when they are used in a ViewModel that does not lose it's state over rotation, every time the Views do Subscribe to them in the onCreate() method, they will be notified with the last available value in the Property ... if you dont want this behavior you can use "Consumable.java" which acts as a one-time storage to a value, once the value in it is consumed (or emitted in onNext()), it returns back to null and does not emit / notify with any values unless it is updated again ... an example of the MVVM pattern with Properties is as follows 

# Create the ViewModel that keeps it's state while rotating

There is a Gist i wrote that shows how to make the Model (ViewModel parent class) survive rotation : https://gist.github.com/Ahmed-Adel-Ismail/c0ec1ed6c8d37c931b3bf42b22430246

now The ViewModel will look like this :

    public class MainViewModel extends Model {
    
        final Property<String> textViewLabel = new Property<>();
        final Consumable<String> toastMessage = new Consumable<>("started");

        final String randomLabel() {
            return (int) (Math.random() * 10) % 2 == 0
                    ? "Even number label"
                    : "Odd number label";
        }

        @Override
        protected void clear() {
            textViewLabel.clear();
            toastMessage.clear();
        }
    }

and our Activity will look like this :

    public class MainActivity extends AppCompatActivity {

        private MainViewModel viewModel;
        private TextView textView;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            textView = (TextView) findViewById(R.id.text_view);
            viewModel = Model.of(this, MainViewModel.class);
            
            // notice that we set the textViewLabel value before we subscribe any views
            viewModel.textViewLabel.set(viewModel.randomLabel()); 
            
            // now as soon as we subscribe to the textViewLabel, it will emit the stored value in it
            viewModel.textViewLabel.asObservable().subscribe(updateTextView());
            
            // since the toastMessage is Consumable, it will emit the value in it just one time ,
            // and after this in every rotation nothing will happen, unlike the textViewLabel that 
            // will still hold the value in it across rotations
            viewModel.toastMessage.asObservable().subscribe(showToast());
        }

        @NonNull
        private Consumer<String> updateTextView() {
            return new Consumer<String>()
            {
                @Override
                public void accept(String s) throws Exception {
                    textView.setText(s);
                }
            };
        }

        private Consumer<? super String> showToast() {
            return new Consumer<String>()
            {
                @Override
                public void accept(String s) throws Exception {
                    Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                }
            };
        }

        @Override
        protected void onDestroy() {
            // this clear wont actually take effect unless the Activity is actually destroyed, not rotating
            viewModel.clear(this);
            textView = null;
            super.onDestroy();
        }
    }


This means that we wont have to care about checking the Values in Properties any more, they are similar to a "ReplaySubject" but with only one item, and it can maintain it's state properly, and it's Property.clear() method handles every thing

notice that in the previous example we made Disposables and they should have disposed them in onDestroy(), but this is not put to focus on the main idea

# Gradle Dependency

Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
    
Step 2. Add the dependency
	
    dependencies {
	        compile 'com.github.Ahmed-Adel-Ismail:RxProperties:0.0.1'
	}
