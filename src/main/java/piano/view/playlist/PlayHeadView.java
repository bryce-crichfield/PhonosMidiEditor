package piano.view.playlist;

import javafx.beans.property.DoubleProperty;
import javafx.scene.shape.Rectangle;
import piano.EditorContext;
import piano.model.GridInfo;

public class PlayHeadView extends Rectangle {
    public PlayHeadView(DoubleProperty height, EditorContext context) {
        super();

        this.setFill(javafx.scene.paint.Color.CYAN);
        this.setStroke(javafx.scene.paint.Color.WHITE);
        this.setOpacity(0.5);
        this.setWidth(10);
        this.heightProperty().bind(height);

        context.getPlayback().observe((old, now) -> {
            GridInfo gi = context.getViewSettings().getGridInfo();
            double x = now.getValue() * gi.getCellWidth();
            this.setTranslateX(x);
            this.toFront();
        });
    }
}
