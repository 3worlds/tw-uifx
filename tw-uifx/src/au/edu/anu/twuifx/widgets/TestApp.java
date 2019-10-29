package au.edu.anu.twuifx.widgets;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class TestApp extends Application {

	@Override
	public void start(Stage primaryStage) throws Exception {

		final BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(1);
		final LongProperty lastUpdate = new SimpleLongProperty();
		final long minUpdateInterval = 0;
		
		TextArea console = new TextArea();
		AnimationTimer timer = new AnimationTimer() {

			@Override
			public void handle(long now) {
				if (now-lastUpdate.get()>minUpdateInterval) {
					final String message = messageQueue.poll();
					if (message!= null) {
						console.appendText("\n"+ message);
					}
					lastUpdate.set(now);
				}
				
			}
		};

		Button startButton = new Button("Start");
		startButton.setOnAction(event -> {
			MessageProducer producer = new MessageProducer(messageQueue);
			Thread t = new Thread(producer);
			t.setDaemon(true);
			t.start();
		});
		
		timer.start();
		
		HBox controls = new HBox(5, startButton);
		controls.setAlignment(Pos.CENTER);
		BorderPane root = new BorderPane(console,null,null,controls,null);
		Scene scene = new Scene(root,600,600);
		primaryStage.setScene(scene);
		primaryStage.show();
		

	}

	private static class MessageProducer implements Runnable {
		private final BlockingQueue<String> messageQueue;

		public MessageProducer(BlockingQueue<String> messageQueue) {
			this.messageQueue = messageQueue;
		}

		@Override
		public void run() {
			long messageCount = 0;
			try {
				while(true) {
					final String message = "Message "+(++messageCount);
					messageQueue.put(message);
				}
			} catch (InterruptedException exc) {
				System.out.println("Message producer interrupted: exiting.");
			}

		}
	};

	public static void main(String[] args) {
		launch(args);
	}
}