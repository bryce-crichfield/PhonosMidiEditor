package piano.view.parameter;

import javafx.collections.ListChangeListener;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import piano.EditorContext;
import piano.Util;
import piano.control.NoteService;
import piano.model.NoteEntry;

import java.util.concurrent.atomic.AtomicReference;

class NoteParameterView extends Rectangle {
    private final EditorContext context;
    private final Pane parent;
    private final NoteEntry noteEntry;

    public NoteParameterView(Pane parent, NoteEntry note, EditorContext context) {
        super();
        this.context = context;
        this.parent = parent;
        this.noteEntry = note;

        this.setFill(Color.WHITE);

        // when note changes, update the circle's position
        note.addListener((observable, oldValue, newValue) -> {
            recalculateViewFromModel();
        });

        // when gridInfo changes, update the circle's position
        context.getViewSettings().gridInfoProperty().addListener((observable, oldValue, newValue) -> {
            recalculateViewFromModel();
        });

        // when the parent's height changes, update the circle's position
        parent.heightProperty().addListener((observable, oldValue, newValue) -> {
            recalculateViewFromModel();
        });

        recalculateViewFromModel();

        this.setOnMouseMoved(event -> {
            // Determine which handle the mouse is over
            // if we are in the top 25% of the rectangle, we are in the top handle
            // otherwise, we are in the body
            double y = event.getY();
            double height = this.getHeight();


            // Change the cursor to indicate which handle we are over
            this.getScene().setCursor(javafx.scene.Cursor.N_RESIZE);
        });

        this.setOnMouseEntered(event -> {
            this.setStroke(Color.BLACK);
        });

        this.setOnMouseExited(event -> {
            this.setStroke(Color.TRANSPARENT);
            this.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
        });


        // I don't really know why we have to track the start position of the mouse drag, but it works.
        AtomicReference<Double> startY = new AtomicReference<>((double) 0);
        this.setOnMousePressed(event -> {
            startY.set(event.getY());
        });

        this.setOnMouseDragged(event -> {
            // The mouse drag is relative to the start position of the mouse drag, so we need to calculate the
            // difference between the current mouse position and the start position of the mouse drag to get the
            // delta.
            double dy = event.getY() - startY.get();
            double ty = this.getTranslateY() + dy;
            double velocity = Util.clamp(1 - (ty / parent.getHeight()), 0, 1);

            context.getNotes().modify(noteEntry, data -> data.withVelocity((int) (velocity * 100)));
        });

        // Sheet Metal gradient
        this.setFill(Color.DARKGREEN.darker());

        this.setArcHeight(10);
        this.setArcWidth(10);

        // When the note is selected, change the color of the rectangle
        NoteService noteService = context.getNotes();
        noteService.getSelectedEntries().addListener((ListChangeListener<? super NoteEntry>) c -> {
            if (noteService.getSelectedEntries().contains(noteEntry)) {
                this.setFill(Color.BLUE);
            } else {
                this.setFill(Color.DARKGREEN.darker());
            }
        });
    }

    private void recalculateViewFromModel() {
        var note = this.noteEntry.get();

        var gridInfo = context.getViewSettings().getGridInfo();

        double x = note.calcXPosOnGrid(gridInfo);
        double y = (1 - note.getVelocityAsPercentage()) * parent.getHeight();

        this.setTranslateX(x);
        this.setTranslateY(y);
        this.setWidth(gridInfo.getCellWidth());
        this.setHeight(parent.getHeight());
    }

    public NoteEntry getNoteEntry() {
        return noteEntry;
    }
}
