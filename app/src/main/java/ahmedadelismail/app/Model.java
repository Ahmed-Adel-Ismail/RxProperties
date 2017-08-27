package ahmedadelismail.app;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import java.lang.reflect.Constructor;

/**
 * the parent class for all Model classes per application
 *
 * Created by Ahmed Adel Ismail on 8/28/2017.
 */
public class Model extends Fragment
{

    @Override
    @CallSuper
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /**
     * create an instance of the passed {@link Model} sub-class, if the instance already
     * exists, it will be returned back
     * @param activity the host {@link AppCompatActivity}
     * @param modelClass the {@link Class} of the sub-class of the {@link Model}
     * @param <T> the expected sub-class type
     * @return the valid {@link Model} instance
     * @throws RuntimeException if the initialization operation failed, make sure that all the
     * {@link Model} sub-classes have default no-args constructor
     * (or no declared constructors at all)
     */
    @SuppressWarnings("unchecked")
    public static <T extends Model> T of(AppCompatActivity activity, Class<T> modelClass)
            throws RuntimeException {
        FragmentManager fm = activity.getSupportFragmentManager();
        T viewModel = (T) fm.findFragmentByTag(modelClass.getName());

        if (viewModel == null) {
            viewModel = createNewInstance(fm, modelClass);
        }

        return viewModel;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Model> T createNewInstance(FragmentManager fm, Class<T> modelClass) {
        T viewModel = (T) constructModel(modelClass);
        fm.beginTransaction()
                .add(viewModel, modelClass.getName())
                .commitAllowingStateLoss();
        return viewModel;
    }

    private static Object constructModel(Class<?> modelClass) {
        try {
            Constructor constructor = modelClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * clear the current {@link Model} from memory
     * @param activity the host {@link AppCompatActivity}
     */
    public final void clear(AppCompatActivity activity) {
        if (activity != null && activity.isFinishing()) {
            doRemoveAndClearSubClasses(activity);
        }
    }

    private void doRemoveAndClearSubClasses(AppCompatActivity activity) {
        FragmentManager fm = activity.getSupportFragmentManager();
        if (!fm.isDestroyed()) {
            fm.beginTransaction().remove(this).commitAllowingStateLoss();
        }
        clear();
    }

    /**
     * override this method by sub-classes to clear the references held by this instance
     */
    protected void clear() {

    }


}
