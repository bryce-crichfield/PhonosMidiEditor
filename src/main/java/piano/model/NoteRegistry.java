package piano.model;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class NoteRegistry {
    private final ObservableList<NoteEntry> notes;

    public NoteRegistry() {
        notes = FXCollections.observableArrayList();
    }

    public NoteEntry register(NoteData note) {
        var entry = new NoteEntry(note);
        notes.add(entry);
        return entry;
    }

    public void unregister(NoteEntry note) {
        notes.remove(note);
    }
    public void onAdded(Consumer<NoteEntry> callback) {
        notes.addListener((ListChangeListener<NoteEntry>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (NoteEntry noteEntry : change.getAddedSubList()) {
                        callback.accept(noteEntry);
                    }
                }
            }
        });
    }

    public void onRemoved(Consumer<NoteEntry> callback) {
        notes.addListener((ListChangeListener<NoteEntry>) change -> {
            while (change.next()) {
                if (change.wasRemoved()) {
                    for (NoteEntry noteEntry : change.getRemoved()) {
                        callback.accept(noteEntry);
                    }
                }
            }
        });
    }

    public Collection<NoteEntry> getEntries() {
        return notes;
    }
}
