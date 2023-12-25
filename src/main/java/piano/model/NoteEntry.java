package piano.model;

import javafx.beans.property.SimpleObjectProperty;

import java.util.function.Function;

public class NoteEntry extends SimpleObjectProperty<NoteData> {
    public NoteEntry(NoteData note) {
        super(note);
    }

    public void modify(Function<NoteData, NoteData> modifier) {
        set(modifier.apply(get()));
    }
}