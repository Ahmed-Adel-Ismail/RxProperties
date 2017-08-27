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

you can add your own code inside the set() and get() methods through methods like filter(), onUpdate(), and onGet()

an example for a property that accepts only even numbers :

    Property<Integer> evenNumbers = new Property<>(0);
    evenNumbers.filter(new Predicate<Integer>() {
        @Override
        public boolean test(@NonNull Integer integer) throws Exception {
            return integer % 2 == 0;
        }
    })


# RxJava2 Observable, Maybe & Observer 

Properties act as an Observable, Maybe and Observer, an example is as follows :

    Property<Integer> observable = new Property<>(10);
    Property<Integer> observer = new Property<>(0);    
    observable.asObservable().subscribe(observer);
    
Properties also have methods like asMaybe(), which returns the Property as a Maybe, which will emit the item in the Property if it is available, or will invoke Maybe.empty() if it has no value in it yet


    
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
