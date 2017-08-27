package ahmedadelismail.app;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity
{

    private final CompositeDisposable disposables = new CompositeDisposable();
    private final MainViewModel viewModel = new MainViewModel();
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.text_view);

        disposables.add(viewModel.textViewLabel.asObservable().subscribe(updateTextView()));
        disposables.add(viewModel.toastMessage.asObservable().subscribe(showToast()));

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

    protected void onResume() {
        super.onResume();
        viewModel.textViewLabel.set(viewModel.randomLabel());
    }


    @Override
    protected void onDestroy() {
        disposables.clear();
        viewModel.clear();
        textView = null;
        super.onDestroy();
    }
}
