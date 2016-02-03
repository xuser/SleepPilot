package view;

import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.MouseEvent;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class FXPopUp {

    private ProgressBar pgB;

    Popup createPopup(final String message) {
        final Popup popup = new Popup();
        popup.setAutoFix(true);
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        Label label = new Label(message);
        label.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                popup.hide();
            }
        });

        label.getStylesheets().add(FXPopUp.class.getResource("Application.css").toExternalForm());
        label.getStyleClass().add("popup");
        popup.getContent().add(label);
        return popup;
    }

    Popup createPopupProgressBar() {
        final Popup popup = new Popup();
//	    popup.setAutoFix(true);
//	    popup.setAutoHide(true);
//	    popup.setHideOnEscape(true);

        pgB = new ProgressBar();
//	    pgB.setOnMouseReleased(new EventHandler<MouseEvent>() {
//	        @Override
//	        public void handle(MouseEvent e) {
//	            popup.hide();
//	        }
//	    });

        pgB.getStylesheets().add(FXPopUp.class.getResource("Application.css").toExternalForm());
        pgB.getStyleClass().add("popupWithProgress");
        popup.getContent().add(pgB);
        return popup;
    }

    public void showPopupMessage(final String message, final Stage stage) {
        final Popup popup = createPopup(message);
        popup.setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                popup.setX(stage.getX() + stage.getWidth() / 2 - popup.getWidth() / 2);
                popup.setY(stage.getY() + stage.getHeight() / 2 - popup.getHeight() / 2);
            }
        });
        popup.show(stage);
    }

    Popup showPopupWithProgressBar(final Stage stage) {
        final Popup popup = createPopupProgressBar();
        popup.setOnShown(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                popup.setX(stage.getX() + stage.getWidth() / 2 - popup.getWidth() / 2);
                popup.setY(stage.getY() + stage.getHeight() / 2 - popup.getHeight() / 2);
            }
        });
        popup.show(stage);

        return popup;
    }

    public void setProgress(double progress) {
        pgB.setProgress(progress);
    }
    

}
