package ahmedadelismail.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.lang.reflect.Constructor;

import io.reactivex.properties.exceptions.RuntimeExceptionConverter;

/**
 * the parent class for all View-Model classes
 *
 * Created by Ahmed Adel Ismail on 8/28/2017.
 */
public abstract class ViewModel extends Fragment
{

    /**
     * create an instance of the passed {@link ViewModel} sub-class, if the instance already
     * exists, it will be returned back
     * @param fm the {@link FragmentManager}
     * @param modelClass the {@link Class} of the sub-class of the {@link ViewModel}
     * @param <T> the expected sub-class type
     * @return the valid {@link ViewModel} instance
     */
    @SuppressWarnings("unchecked")
    public static <T extends ViewModel> T of(FragmentManager fm, Class<T> modelClass) {
        T viewModel = (T) fm.findFragmentByTag(modelClass.getName());

        if (viewModel == null) {
            viewModel = createNewInstance(fm, modelClass);
        }

        return viewModel;
    }

    @SuppressWarnings("unchecked")
    private static <T extends ViewModel> T createNewInstance(FragmentManager fm, Class<T> modelClass) {
        T viewModel = (T) constructViewModel(modelClass);
        fm.beginTransaction()
                .add(viewModel, modelClass.getName())
                .commitAllowingStateLoss();
        return viewModel;
    }

    private static Object constructViewModel(Class<?> modelClass) {
        try {
            Constructor constructor = modelClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeExceptionConverter().apply(e);
        }
    }

    @Override
    public final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
}
