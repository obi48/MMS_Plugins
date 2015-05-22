package TestPlugin;

import com.sun.javafx.Utils;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;

public class SpectrumBar extends VBox {
	int maxValue;
	int barCount;
	double lastWidth = 0.0;
	double lastHeight = 0.0;

	public SpectrumBar(int maxValue, int barCount) {
		this.maxValue = maxValue;
		this.barCount = barCount;

		setSpacing(1.0);
		setAlignment(Pos.BOTTOM_CENTER);
		setStyle("-fx-background-color: black; -fx-padding: 2;");

		Stop[] stop = new Stop[5];
		stop[0] = new Stop(0.2, Color.RED);
		stop[1] = new Stop(0.4, Color.ORANGE);
		stop[2] = new Stop(0.6, Color.YELLOW);
		stop[3] = new Stop(0.8, Color.LIGHTGREEN);
		stop[4] = new Stop(0.9, Color.GREEN);

		//init rectangles
		for (int i = 0; i < barCount; i++) {
			int color = (int) ((double) i / (double) barCount * 255.0);
			Rectangle rectangle = new Rectangle(20,5);
			rectangle.setVisible(false);
			rectangle.setFill(Utils.ladder(Color.rgb(color, color, color), stop));
			getChildren().add(rectangle);
		}
	}

	public void setValue(double value) {
		int maxVisible = Math.min(barCount, (int) Math.round(value / maxValue * barCount));
		ObservableList<Node> bars = getChildren();
		for (int i = 0; i < bars.size(); i++) {
			bars.get(i).setVisible(i > barCount - maxVisible);
		}
	}
}