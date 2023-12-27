package piano.view.midi;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import piano.EditorContext;
import piano.model.GridInfo;
import piano.model.NoteData;
import piano.model.NoteEntry;
import piano.tool.EditorTool;
import piano.tool.PencilTool;

import java.util.Optional;

public class NoteMidiView extends StackPane {
    private final EditorContext context;
    private final NoteEntry noteEntry;
    private final Rectangle rectangle;
    private final Text label;
    private final NoteMidiHandle.Left leftHandle;
    private final NoteMidiHandle.Right rightHandle;
    private final NoteMidiHandle.Body bodyHandle;
    private Optional<NoteMidiHandle> selectedHandle;
    private NoteMidiController selfController = new PencilNoteMidiController();
    ObjectProperty<Optional<EditorTool>> currentTool;

    public NoteMidiView(NoteEntry noteEntry, EditorContext context, ObjectProperty<Optional<EditorTool>> currentTool) {
        super();
        this.context = context;
        this.noteEntry = noteEntry;
        this.currentTool = currentTool;

        label = new Text("");
        rectangle = new Rectangle();
        rectangle.setFill(Color.DARKGREEN);
        rectangle.setStroke(Color.BLACK);
        rectangle.setStrokeWidth(1);
        rectangle.setArcHeight(10);
        rectangle.setArcWidth(10);
        getChildren().addAll(rectangle, label);

        bindPaneToRectangle();

        // When the grid changes, we need to update the view
        context.getViewSettings().gridInfoProperty().addListener((observable1, oldValue1, newValue1) -> {
            NoteData data = this.noteEntry.get();
            GridInfo grid = newValue1;

            double x = data.calcXPosOnGrid(grid);
            double y = data.calcYPosOnGrid(grid);
            rectangle.setX(x);
            rectangle.setY(y);

            double width = (data.getEnd() - data.getStart()) * grid.getCellWidth();
            rectangle.setWidth(width);

            double height = grid.getCellHeight();
            rectangle.setHeight(height);
        });

        currentTool.addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                return;
            }

            if (newValue.get() instanceof PencilTool) {
                System.out.println("Pencil tool");
                selfController = new PencilNoteMidiController();
            } else {
                System.out.println("Unknown tool");
                selfController = new NoteMidiController() {};
            }
        });

        // Set handle on mouse hover
        rectangle.setOnMouseMoved(event -> selfController.rectangleOnMouseMoved(event));

        // Delegate the drag event to the selected handle
        this.setOnMouseDragged(event -> selfController.stackPaneOnMouseDragged(event));

        // Update the view rectangle when the note data model changes
        noteEntry.addListener((observable, oldValue, newValue) -> {
            var gridInfo = context.getViewSettings().gridInfoProperty();
            double x = newValue.calcXPosOnGrid(gridInfo.get());
            double y = newValue.calcYPosOnGrid(gridInfo.get());
            rectangle.setX(x);
            rectangle.setY(y);

            double width = (newValue.getEnd() - newValue.getStart()) * gridInfo.get().getCellWidth();
            rectangle.setWidth(width);

            double height = gridInfo.get().getCellHeight();
            rectangle.setHeight(height);
        });

        // Update the view rectangle when the selected entries change
        context.getNotes().getSelectedEntries().addListener((ListChangeListener<NoteEntry>) c -> {
            if (c.getList().contains(noteEntry)) {
                rectangle.setFill(Color.BLUE);
            } else {
                rectangle.setFill(Color.DARKGREEN);
            }
        });

        // Initialize the view
        NoteData data = this.noteEntry.get();
        var gridInfo = context.getViewSettings().gridInfoProperty();
        double x = data.calcXPosOnGrid(gridInfo.get());
        double y = data.calcYPosOnGrid(gridInfo.get());
        rectangle.setX(x);
        rectangle.setY(y);
        double width = (data.getEnd() - data.getStart()) * gridInfo.get().getCellWidth();
        rectangle.setWidth(width);
        double height = gridInfo.get().getCellHeight();
        rectangle.setHeight(height);

        // Create handles
        leftHandle = new NoteMidiHandle.Left(this, context, noteEntry, rectangle);
        rightHandle = new NoteMidiHandle.Right(this, context, noteEntry, rectangle);
        bodyHandle = new NoteMidiHandle.Body(this, context, noteEntry, rectangle);
    }

    private void bindPaneToRectangle() {
        rectangle.xProperty().addListener((observable, oldValue, newValue) -> {
            label.setX(newValue.doubleValue() + rectangle.getWidth() / 2 - label.getLayoutBounds().getWidth() / 2);
            this.setLayoutX(newValue.doubleValue());
        });

        rectangle.yProperty().addListener((observable, oldValue, newValue) -> {
            label.setY(newValue.doubleValue() + rectangle.getHeight() / 2 + label.getLayoutBounds().getHeight() / 2);
            this.setLayoutY(newValue.doubleValue());

            NoteData noteData = this.noteEntry.get();
            String noteString = NoteData.noteToString(88 - noteData.getNote());
            label.setText(noteString);
        });

        rectangle.widthProperty().addListener((observable, oldValue, newValue) -> {
            label.setX(newValue.doubleValue() / 2 - label.getLayoutBounds().getWidth() / 2);
            label.setY(newValue.doubleValue() / 2 + label.getLayoutBounds().getHeight() / 2);
            this.setWidth(newValue.doubleValue());
        });
    }

    public NoteEntry getNoteEntry() {
        return noteEntry;
    }


    private class PencilNoteMidiController implements NoteMidiController {
        @Override
        public void stackPaneOnMouseDragged(MouseEvent event) {
            double deltaX = event.getX();
            double deltaY = event.getY();

            var gridInfo = context.getViewSettings().gridInfoProperty();

            double cellX = deltaX / gridInfo.get().getCellWidth();
            double cellY = deltaY / gridInfo.get().getCellHeight();

            selectedHandle.ifPresent(handle -> handle.onDragged(cellX, cellY));
        }

        @Override
        public void rectangleOnMouseMoved(MouseEvent event) {
            double mouseX = event.getX();

            if (leftHandle.isHovered(mouseX)) {
                selectedHandle = Optional.of(leftHandle);
            } else if (rightHandle.isHovered(mouseX)) {
                selectedHandle = Optional.of(rightHandle);
            } else if (bodyHandle.isHovered(mouseX)) {
                selectedHandle = Optional.of(bodyHandle);
            } else {
                selectedHandle = Optional.empty();
            }

            // Change the cursor to indicate the handle that will be selected
            selectedHandle.ifPresent(handle -> setCursor(handle.getCursor()));
        }
    }
}