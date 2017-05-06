package io.reactivex.properties;

/**
 * a {@link Property} for {@code boolean} values
 * <p>
 * Created by Ahmed Adel on 12/28/2016.
 */
public class BooleanProperty extends Property<Boolean> {

    public BooleanProperty(boolean object) {
        super(object);
    }

    /**
     * check if the {@code boolean} stored is {@code true} or not
     *
     * @return {@code true} of the stored object is {@code true}, else {@code false}
     */
    public boolean isTrue() {
        return object != null && object;
    }


}
