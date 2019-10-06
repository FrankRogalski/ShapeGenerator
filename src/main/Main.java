package main;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import main.vectors.Vector2D;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class Main extends Application {
    private final Random random = new Random();
    private Canvas canvas;
    private PixelWriter pixelWriter;
    private Timeline tlDraw;

    private int x;
    private int y;
    private static final int FPS = 10000;
    private double multiplier = 0.5;
    private List<Vector2D> vectors;

    @Override
    public void init() {
        tlDraw = new Timeline(new KeyFrame(Duration.millis(1000d / FPS), e -> gameLoop()));
        tlDraw.setCycleCount(Animation.INDEFINITE);
    }

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Scene scene = new Scene(root, 1000, 800);

        primaryStage.setTitle("Triangle");

        canvas = new Canvas(scene.getWidth(), scene.getHeight());
        final GraphicsContext gc = canvas.getGraphicsContext2D();

        root.getChildren().add(canvas);

        final Label edgeName = new Label("Edges: ");
        final Slider edges = new Slider(2, 10, 3);
        edges.setShowTickMarks(true);
        edges.setShowTickLabels(true);
        edges.setSnapToTicks(true);
        edges.setMajorTickUnit(1);
        edges.setMinorTickCount(0);
        edges.setLayoutX(40);
        edges.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() != oldValue.intValue()) {
                vectors = getPolygon(newValue.intValue());
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            }
        });

        final Label multiplierName = new Label("Multiplier: ");
        multiplierName.setLayoutY(40);
        final Slider multiplierSlider = new Slider(0, 1, multiplier);
        multiplierSlider.setShowTickMarks(true);
        multiplierSlider.setShowTickLabels(true);
        multiplierSlider.setSnapToTicks(true);
        multiplierSlider.setMajorTickUnit(0.25);
        multiplierSlider.setMinorTickCount(4);
        multiplierSlider.setLayoutX(60);
        multiplierSlider.setLayoutY(40);
        multiplierSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            multiplier = newValue.doubleValue();
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        });

        final Rectangle rectangle = new Rectangle(200, 80, Color.WHITE);

        root.getChildren().addAll(rectangle, edgeName, edges, multiplierName, multiplierSlider);

        scene.widthProperty().addListener((observableValue, oldVal, newVal) -> {
            canvas.setWidth(newVal.doubleValue());
            vectors = getPolygon((int) edges.getValue());
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        });
        scene.heightProperty().addListener((observableValue, oldVal, newVal) -> {
            canvas.setHeight(newVal.doubleValue());
            vectors = getPolygon((int) edges.getValue());
            gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        });

        primaryStage.setScene(scene);
        primaryStage.show();

        vectors = getPolygon((int) edges.getValue());
        final Vector2D vector2D = vectors.get(random.nextInt(vectors.size()));

        x = (int) vector2D.getX();
        y = (int) vector2D.getY();

        pixelWriter = gc.getPixelWriter();

        tlDraw.play();
    }

    private void gameLoop() {
        pixelWriter.setColor(x, y, Color.BLACK);

        final Vector2D vector2D = vectors.get(random.nextInt(vectors.size()));
        x -= (int) ((x - vector2D.getX()) * multiplier);
        y -= (int) ((y - vector2D.getY()) * multiplier);
    }

    public static void main(String[] args) {
        launch(args);
    }

    private List<Vector2D> getPolygon(final int numPoints) {
        final double angle = Math.PI * 2 / numPoints;
        final List<Vector2D> vector2DS = new ArrayList<>(numPoints);
        Vector2D last = new Vector2D(0, 0);
        for (int i = 0; i < numPoints; i++) {
            final Vector2D vector2D = Vector2D.add(Vector2D.createFromAngle(Math.PI + angle * i), last);
            last = vector2D;
            vector2DS.add(vector2D);
        }

        final double minX = vector2DS.stream().min(Comparator.comparing(Vector2D::getX)).map(Vector2D::getX).orElseThrow(RuntimeException::new);
        final double maxX = vector2DS.stream().max(Comparator.comparing(Vector2D::getX)).map(Vector2D::getX).orElseThrow(RuntimeException::new);
        final double minY = vector2DS.stream().min(Comparator.comparing(Vector2D::getY)).map(Vector2D::getY).orElseThrow(RuntimeException::new);
        final double maxY = vector2DS.stream().max(Comparator.comparing(Vector2D::getY)).map(Vector2D::getY).orElseThrow(RuntimeException::new);

        final double difX = maxX - minX;
        final double difY = maxY - minY;
        final double factor = canvas.getHeight() / canvas.getWidth() > difY / difX ? canvas.getWidth() / difX : canvas.getHeight() / difY;
        final double addX = (canvas.getWidth() - difX * factor) / 2;
        final double addY = (canvas.getHeight() - difY * factor) / 2;
        final Vector2D adder = new Vector2D(addX - minX * factor, addY - minY * factor);

        vector2DS.forEach(vector2D -> vector2D.multiply(factor));
        vector2DS.forEach(vector2D -> vector2D.add(adder));
        return vector2DS;
    }
}
