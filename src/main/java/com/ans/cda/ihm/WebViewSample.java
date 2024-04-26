package com.ans.cda.ihm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ans.cda.constant.Constant;
import com.ans.cda.service.bom.BomService;
import com.ans.cda.service.crossvalidation.CrossValidationService;
import com.ans.cda.service.validation.ValidationService;
import com.ans.cda.service.xdm.IheXdmService;
import com.ans.cda.service.xdm.XdmService;
import com.ans.cda.utilities.general.LocalUtility;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;

/**
 * WebViewSample api with JavaFX
 * 
 * @author bensalem Nizar
 */
public class WebViewSample extends Application {
	/**
	 * browser
	 */
	private final WebView browserEngine = new WebView();
	/**
	 * FILENAME
	 */
	private static final String FILENAME = System.getProperty("user.home");
	/**
	 * webEngine
	 */
	private final WebEngine webEngine = browserEngine.getEngine();
	/**
	 * imageViewAns
	 */
	private ImageView imageViewAns;
	/**
	 * textFieldCda
	 */
	private TextField textFieldCda = new TextField();
	/**
	 * labelCda
	 */
	private Label labelCda = new Label("Sélection du fichier CDA");
	/**
	 * labelMeta
	 */
	private Label labelMeta = new Label("Sélection du fichier META");
	/**
	 * textFieldMeta
	 */
	public TextField textFieldMeta = new TextField();
	/**
	 * view57
	 */
	private ImageView view57 = new ImageView(Constant.POINT);
	/**
	 * 
	 */
	private URL url = this.getClass().getClassLoader().getResource(Constant.INTEROPFILE);
	/**
	 * Logger
	 */
	private static final Logger LOG = Logger.getLogger(WebViewSample.class);

	/**
	 * void main for Javafx launcher Main secondaire de l'application de javaFX
	 * 
	 * @param args
	 */

	public static void main(final String args[]) {
		launch(args);
	}

	/**
	 * loading in api
	 */
	public void runTask(final Stage taskUpdateStage, final ProgressIndicator progress) {
		final Task<Void> longTask = new Task<>() {
			@Override
			protected Void call() throws Exception {
				final int max = 100;
				for (int i = 1; i <= max; i++) {
					if (isCancelled()) {
						break;
					}
					updateProgress(i, max);
				}
				return null;
			}
		};
		longTask.setOnSucceeded(new EventHandler<>() {
			@Override
			public void handle(final WorkerStateEvent event) {
				taskUpdateStage.hide();
			}
		});
		progress.progressProperty().bind(longTask.progressProperty());
		taskUpdateStage.show();
		new Thread(longTask).start();
	}

	/**
	 * start stage
	 * 
	 * @param stage
	 */
	@Override
	public void start(final Stage stage) {
		// Start ProgressBar creation
		final double wndwWidth = 150.0d;
		final double wndhHeigth = 150.0d;
		final ProgressIndicator progress = new ProgressIndicator();
		progress.setMinWidth(wndwWidth);
		progress.setMinHeight(wndhHeigth);
		progress.setProgress(0.25F);
		final VBox updatePane = new VBox();
		updatePane.setPadding(new Insets(10));
		updatePane.setSpacing(5.0d);
		updatePane.setAlignment(Pos.CENTER);
		LocalUtility.getChildrenNode(updatePane).addAll(progress);
		updatePane.setStyle("-fx-background-color: transparent");
		final Stage taskUpdateStage = new Stage(StageStyle.UNDECORATED);
		taskUpdateStage.setScene(new Scene(updatePane, 170, 170));
		// End progressBar
		final VBox vBox = new VBox(10);
		vBox.setMinSize(stage.getMinWidth(), stage.getMinHeight());
		vBox.setStyle("-fx-background-color: transparent;");
		final Image ansImage = new Image(WebViewSample.class.getResource("/images/ans01.jpg").toExternalForm());
		// creating ImageView for adding image
		imageViewAns = new ImageView();
		imageViewAns.setImage(ansImage);
		imageViewAns.setFitWidth(100);
		imageViewAns.setFitHeight(100);
		imageViewAns.setPreserveRatio(true);
		imageViewAns.setSmooth(true);
		imageViewAns.setCursor(Cursor.HAND);
		final Tooltip tooltip = new Tooltip(Constant.URL);
		Tooltip.install(imageViewAns, tooltip);
		imageViewAns.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<>() {
			@Override
			public void handle(final MouseEvent event) {
				LocalUtility.browser(Constant.URL);
				event.consume();
			}
		});

		// creating HBox to add imageview
		final HBox hBoxImg = new HBox();
		LocalUtility.getChildrenHNode(hBoxImg).addAll(imageViewAns);
		hBoxImg.setStyle("-fx-background-color: white;");
		hBoxImg.setMaxSize(HBox.USE_PREF_SIZE, HBox.USE_PREF_SIZE);

		final HBox hBoxImg1 = new HBox();
		LocalUtility.getChildrenHNode(hBoxImg1).addAll();
		hBoxImg1.setStyle("-fx-background-color: white;");
		hBoxImg1.setMaxSize(HBox.USE_PREF_SIZE, HBox.USE_PREF_SIZE);

		final VBox vBoxImg = new VBox();
		vBoxImg.getChildren().addAll(hBoxImg, hBoxImg1);

		final FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Choix du fichier");
		LocalUtility.newExtFilter(fileChooser).addAll(new ExtensionFilter("CDA", "*.xml"));

		final FileChooser fileChooser1 = new FileChooser();
		fileChooser1.setTitle("Choix du fichier");
		LocalUtility.newExtFilter(fileChooser1).addAll(new ExtensionFilter("META", "*.xml"));

		final Button button1 = new Button();
		final ImageView view = new ImageView(Constant.FOLDERPHOTO);
		button1.setGraphic(view);
		button1.setStyle(Constant.STYLE1);
		button1.setPrefSize(70, 40);
		button1.setMinSize(70, 40);
		button1.setMaxSize(70, 40);
		final Tooltip tooltip1 = new Tooltip("Ouvrir le fichier CDA");
		button1.setTooltip(tooltip1);

		button1.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					final File file = fileChooser.showOpenDialog(stage);
					if (file != null) {
						textFieldCda.setText(file.getAbsolutePath());
					}
				});
			}
		});

		// supprimer BOM CDA
		final Button button01 = new Button();
		final ImageView view01 = new ImageView(Constant.BOM);
		button01.setGraphic(view01);
		button01.setStyle(Constant.STYLE1);
		button01.setPrefSize(70, 40);
		button01.setMinSize(70, 40);
		button01.setMaxSize(70, 40);
		final Tooltip tooltip01 = new Tooltip("Supprimer l'encodage BOM du CDA");
		button01.setTooltip(tooltip01);

		button01.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					try {
						if (!textFieldCda.getText().isEmpty()) {
							BomService.saveAsUTF8WithoutBOM(textFieldCda.getText(), Charset.defaultCharset());
							final Alert alert = new Alert(AlertType.INFORMATION);
							final DialogPane dialogPane = alert.getDialogPane();
							dialogPane.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
							dialogPane.getStyleClass().add(Constant.DIALOG);
							dialogPane.setMinHeight(130);
							dialogPane.setMaxHeight(130);
							dialogPane.setPrefHeight(130);
							alert.setContentText("Traitement terminé avec succès");
							alert.setHeaderText(null);
							alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
							alert.showAndWait();
						} else {
							final Alert alert = new Alert(AlertType.ERROR);
							final DialogPane dialogPane = alert.getDialogPane();
							dialogPane.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
							dialogPane.getStyleClass().add(Constant.DIALOG);
							dialogPane.setMinHeight(130);
							dialogPane.setMaxHeight(130);
							dialogPane.setPrefHeight(130);
							alert.setContentText("Merci de renseigner le fichier CDA");
							alert.setHeaderText(null);
							alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
							alert.showAndWait();
						}
					} catch (final IOException e) {
						if (LOG.isInfoEnabled()) {
							final String error = e.getMessage();
							LOG.error(error);
						}
					}
				});
			}
		});

		// supprimer BOM META
		final Button button02 = new Button();
		final ImageView view02 = new ImageView(Constant.BOM);
		button02.setGraphic(view02);
		button02.setStyle(Constant.STYLE1);
		button02.setPrefSize(70, 40);
		button02.setMinSize(70, 40);
		button02.setMaxSize(70, 40);
		final Tooltip tooltip02 = new Tooltip("Supprimer l'encodage BOM du META");
		button02.setTooltip(tooltip02);

		button02.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					try {
						if (!textFieldMeta.getText().isEmpty()) {
							BomService.saveAsUTF8WithoutBOM(textFieldMeta.getText(), Charset.defaultCharset());
							final Alert alert = new Alert(AlertType.INFORMATION);
							final DialogPane dialogPane = alert.getDialogPane();
							dialogPane.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
							dialogPane.getStyleClass().add(Constant.DIALOG);
							dialogPane.setMinHeight(130);
							dialogPane.setMaxHeight(130);
							dialogPane.setPrefHeight(130);
							alert.setContentText("Traitement terminé avec succès");
							alert.setHeaderText(null);
							alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
							alert.showAndWait();
						} else {
							final Alert alert = new Alert(AlertType.ERROR);
							final DialogPane dialogPane = alert.getDialogPane();
							dialogPane.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
							dialogPane.getStyleClass().add(Constant.DIALOG);
							dialogPane.setMinHeight(130);
							dialogPane.setMaxHeight(130);
							dialogPane.setPrefHeight(130);
							alert.setContentText("Merci de renseigner le fichier META");
							alert.setHeaderText(null);
							alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
							alert.showAndWait();
						}
					} catch (final IOException e) {
						if (LOG.isInfoEnabled()) {
							final String error = e.getMessage();
							LOG.error(error);
						}
					}
				});
			}
		});

		final Button button2 = new Button();
		final ImageView view1 = new ImageView(Constant.FOLDERPHOTO);
		button2.setGraphic(view1);
		button2.setStyle(Constant.STYLE1);
		button2.setPrefSize(70, 40);
		button2.setMinSize(70, 40);
		button2.setMaxSize(70, 40);
		final Tooltip tooltip2 = new Tooltip("Ouvrir le fichier de métadonnées");
		button2.setTooltip(tooltip2);

		button2.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {

					final File file = fileChooser1.showOpenDialog(stage);
					if (file != null) {
						textFieldMeta.setText(file.getAbsolutePath());
					}
				});
			}
		});

		// xml button
		final Button button3 = new Button();
		final ImageView view3 = new ImageView(Constant.XML);
		button3.setGraphic(view3);
		button3.setStyle(Constant.STYLE1);
		button3.setPrefSize(70, 40);
		button3.setMinSize(70, 40);
		button3.setMaxSize(70, 40);
		final Tooltip tooltip3 = new Tooltip("Ouvrir le fichier CDA");
		button3.setTooltip(tooltip3);

		// validate button
		final Button button5 = new Button();
		final ImageView view5 = new ImageView(Constant.VALIDATE);
		button5.setGraphic(view5);
		button5.setStyle(Constant.STYLE1);
		button5.setPrefSize(70, 40);
		button5.setMinSize(70, 40);
		button5.setMaxSize(70, 40);
		final Tooltip tooltip5 = new Tooltip("Valider en ligne le fichier CDA");
		button5.setTooltip(tooltip5);

		final Region spacer1 = new Region();
		spacer1.setMaxWidth(10);
		HBox.setHgrow(spacer1, Priority.ALWAYS);

		// xml button
		final Button button4 = new Button();
		final ImageView view4 = new ImageView(Constant.XML);
		button4.setGraphic(view4);
		button4.setStyle(Constant.STYLE1);
		button4.setPrefSize(70, 40);
		button4.setMinSize(70, 40);
		button4.setMaxSize(70, 40);
		final Tooltip tooltip4 = new Tooltip("Ouvrir le fichier META");
		button4.setTooltip(tooltip4);

		// validate button
		final Button button7 = new Button();
		final ImageView view7 = new ImageView(Constant.VALIDATE);
		button7.setGraphic(view7);
		button7.setStyle(Constant.STYLE1);
		button7.setPrefSize(70, 40);
		button7.setMinSize(70, 40);
		button7.setMaxSize(70, 40);
		final Tooltip tooltip6 = new Tooltip("Valider en ligne le fichier META");
		button7.setTooltip(tooltip6);

		final TextArea textAreaConsole = new TextArea();
		textAreaConsole.setStyle("-fx-font-weight: bold");

		button7.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				if (!textFieldMeta.getText().isEmpty()) {
					runTask(taskUpdateStage, progress);
					Platform.runLater(() -> {
						textAreaConsole.clear();
						final String console = ValidationService.validateMeta(new File(textFieldMeta.getText()),
								Constant.MODEL, Constant.ASIPXDM, Constant.URLVALIDATION);
						textAreaConsole.setText(console);
					});
				} else {
					final Alert alert = new Alert(AlertType.ERROR);
					final DialogPane dialogPane = alert.getDialogPane();
					dialogPane.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					dialogPane.getStyleClass().add(Constant.DIALOG);
					dialogPane.setMinHeight(130);
					dialogPane.setMaxHeight(130);
					dialogPane.setPrefHeight(130);
					alert.setContentText("Merci de renseigner le fichier META");
					alert.setHeaderText(null);
					alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
					alert.showAndWait();
				}
			}
		});

		final Region spacer2 = new Region();
		spacer2.setMaxWidth(10);
		HBox.setHgrow(spacer2, Priority.ALWAYS);

		// XDM button
		final Button button03 = new Button();
		final ImageView view03 = new ImageView(Constant.XDM);
		button03.setGraphic(view03);
		button03.setStyle(Constant.STYLE1);
		button03.setPrefSize(70, 40);
		button03.setMinSize(70, 40);
		button03.setMaxSize(70, 40);
		final Tooltip tooltip06 = new Tooltip("Sélectionner une archive XDM");
		button03.setTooltip(tooltip06);

		button03.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				Platform.runLater(() -> {
					// Sélection du fichier CDA à controler
					final FileChooser fileChooser = new FileChooser();
					fileChooser.setTitle("Choix du fichier");
					LocalUtility.newExtFilter(fileChooser).addAll(new ExtensionFilter("IHE XDM", "*.zip"),
							new ExtensionFilter("All files", "*.*"));
					final File file = fileChooser.showOpenDialog(stage);
					final Map<String, String> path = XdmService.openXDMFile(stage, file);
					Iterator<Entry<String, String>> iterator = path.entrySet().iterator();
					while (iterator.hasNext()) {
						@SuppressWarnings("rawtypes")
						Map.Entry mapentry = (Map.Entry) iterator.next();
						textFieldCda.setText((String) mapentry.getKey());
						textFieldMeta.setText((String) mapentry.getValue());
					}
				});
			}
		});

		// CROSS button
		final Button button04 = new Button();
		final ImageView view04 = new ImageView(Constant.CROSS);
		button04.setGraphic(view04);
		button04.setStyle(Constant.STYLE1);
		button04.setPrefSize(70, 40);
		button04.setMinSize(70, 40);
		button04.setMaxSize(70, 40);
		final Tooltip tooltip04 = new Tooltip("Cross-Valider les fichiers CDA et META");
		button04.setTooltip(tooltip04);
		button04.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				if (!textFieldCda.getText().isEmpty() && !textFieldMeta.getText().isEmpty()) {
					runTask(taskUpdateStage, progress);
					Platform.runLater(() -> {
						textAreaConsole.clear();
						final String console = CrossValidationService.crossValidate(new File(textFieldCda.getText()),
								new File(textFieldMeta.getText()), Constant.URLVALIDATION);
						textAreaConsole.setText(console);
					});
				} else {
					final Alert alert = new Alert(AlertType.ERROR);
					final DialogPane dialogPane = alert.getDialogPane();
					dialogPane.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					dialogPane.getStyleClass().add(Constant.DIALOG);
					dialogPane.setMinHeight(130);
					dialogPane.setMaxHeight(130);
					dialogPane.setPrefHeight(130);
					alert.setContentText("Merci de renseigner le fichier CDA et le fichier META");
					alert.setHeaderText(null);
					alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
					alert.showAndWait();
				}
			}
		});

		// CROSS button
		final Button button05 = new Button();
		final ImageView view05 = new ImageView(Constant.RAPPORT);
		button05.setGraphic(view05);
		button05.setStyle(Constant.STYLE1);
		button05.setPrefSize(70, 40);
		button05.setMinSize(70, 40);
		button05.setMaxSize(70, 40);
		final Tooltip tooltip05 = new Tooltip("Afficher le dernier rapport de validation");
		button05.setTooltip(tooltip05);

		final HBox hbox3 = new HBox();
		hbox3.getChildren().addAll(button04, button05);

		textFieldCda.setMinSize(900, button1.getPrefHeight());
		textFieldMeta.setMinSize(900, button2.getPrefHeight());

		final Region spacer3 = new Region();
		spacer3.setMaxWidth(10);
		HBox.setHgrow(spacer3, Priority.ALWAYS);

		final Region spacer4 = new Region();
		spacer4.setMaxWidth(10);
		HBox.setHgrow(spacer4, Priority.ALWAYS);

		final HBox hbox1 = new HBox();
		hbox1.getChildren().addAll(textFieldCda, button01, spacer1, button1, button3, button5, spacer3, hbox3);

		final HBox hbox2 = new HBox();
		hbox2.getChildren().addAll(textFieldMeta, button02, spacer2, button2, button4, button7, spacer4, button03);

		final HBox hbox4 = new HBox();
		final HBox hbox5 = new HBox();
		final HBox hbox6 = new HBox();
		final HBox hbox7 = new HBox();
		final HBox hbox8 = new HBox();
		final HBox hbox9 = new HBox();
		final HBox hbox10 = new HBox();
		final HBox hbox11 = new HBox();

		final HBox hbox41 = new HBox();
		final HBox hbox42 = new HBox();
		final HBox hbox43 = new HBox();
		final HBox hbox44 = new HBox();
		final HBox hbox45 = new HBox();
		final HBox hbox46 = new HBox();
		final HBox hbox47 = new HBox();
		final HBox hbox48 = new HBox();

		final Button button41 = new Button();
		final ImageView view41 = new ImageView(Constant.META);
		button41.setGraphic(view41);
		button41.setStyle(Constant.STYLE1);
		button41.setPrefSize(70, 40);
		button41.setMinSize(70, 40);
		button41.setMaxSize(70, 40);
		final Tooltip tooltip41 = new Tooltip("Générer un fichier de métadonnées XDM");
		button41.setTooltip(tooltip41);
		button41.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				if (!textFieldCda.getText().isEmpty()) {
					Platform.runLater(() -> {
						final Stage stageTwo = new Stage();
						// open new scene for xdm
						final VBox root = new VBox();
						final Label label = new Label("Liste des fichiers CDA du lot de soumission");
						label.setPadding(new Insets(0, 0, 0, 20));
						final HBox hbox = new HBox();
						final ObservableList<String> names = FXCollections.observableArrayList(textFieldCda.getText());
						final ListView<String> listView = new ListView<String>(names);
						listView.setPrefSize(1200, 100);

						final Button button = new Button();
						final ImageView view = new ImageView(Constant.ADDFILE);
						button.setGraphic(view);
						button.setStyle(Constant.STYLE1);
						button.setPrefSize(40, 40);
						button.setMinSize(40, 40);
						button.setMaxSize(40, 40);
						final Tooltip tooltip = new Tooltip("Ajouter un CDA dans la liste");
						button.setTooltip(tooltip);
						button.setPadding(new Insets(0, 0, 0, 10));

						button.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								Platform.runLater(() -> {
									final FileChooser fileChooserTwo = new FileChooser();
									final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
											"XML files (*.xml)", "*.xml");
									fileChooserTwo.getExtensionFilters().add(extFilter);
									final File file = fileChooserTwo.showOpenDialog(stageTwo);
									if (file != null) {
										listView.getItems().add(file.getAbsolutePath());
									}
								});
							}
						});

						final Button buttonR = new Button();
						final ImageView viewR = new ImageView(Constant.REMOVEFILE);
						buttonR.setGraphic(viewR);
						buttonR.setStyle(Constant.STYLE1);
						buttonR.setPrefSize(40, 40);
						buttonR.setMinSize(40, 40);
						buttonR.setMaxSize(40, 40);
						final Tooltip tooltipR = new Tooltip("Supprimer le CDA sélectionné de la liste");
						buttonR.setTooltip(tooltipR);
						buttonR.setPadding(new Insets(0, 0, 0, 10));

						buttonR.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								Platform.runLater(() -> {
									final int selectedIdx = listView.getSelectionModel().getSelectedIndex();
									if (selectedIdx != -1) {
										listView.getItems().remove(selectedIdx);
									}
								});
							}
						});

						final Region spacer = new Region();
						spacer.setMaxHeight(50);
						VBox.setVgrow(spacer, Priority.ALWAYS);

						final VBox vbox = new VBox();
						hbox.setPadding(new Insets(10, 0, 0, 20));
						vbox.getChildren().addAll(button, spacer, buttonR);

						final HBox hbox1 = new HBox();
						final Label label1 = new Label("Commentaires relatifs au lot de soumission");
						final TextField textField1 = new TextField();
						textField1.setPrefWidth(950);

						final Region spacer1 = new Region();
						spacer1.setMaxWidth(20);
						HBox.setHgrow(spacer1, Priority.ALWAYS);

						hbox1.getChildren().addAll(label1, spacer1, textField1);
						hbox1.setPadding(new Insets(10, 0, 0, 20));

						final HBox hbox2 = new HBox();
						final Label label2 = new Label("TemplateID principal");
						final TextField textField2 = new TextField();
						textField2.setPrefWidth(950);

						final Region spacer2 = new Region();
						spacer2.setMaxWidth(137);
						HBox.setHgrow(spacer2, Priority.ALWAYS);

						hbox2.getChildren().addAll(label2, spacer2, textField2);
						hbox2.setPadding(new Insets(10, 0, 0, 20));

						final HBox hbox3 = new HBox();
						hbox3.getChildren().addAll(label2, spacer2, textField2);
						hbox3.setPadding(new Insets(10, 0, 0, 20));

						final Button buttonMeta = new Button();
						final ImageView viewMeta = new ImageView(Constant.METAFILE);
						buttonMeta.setGraphic(viewMeta);
						buttonMeta.setStyle(Constant.STYLE1);
						buttonMeta.setPrefSize(70, 50);
						buttonMeta.setMinSize(70, 50);
						buttonMeta.setMaxSize(70, 50);
						final Tooltip tooltipMeta = new Tooltip("Générer le métadata");
						buttonMeta.setTooltip(tooltipMeta);
						buttonMeta.setPadding(new Insets(0, 0, 0, 10));

						final TextArea textArea = new TextArea();
						textArea.setStyle(Constant.STYLETX);
						textArea.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());

						buttonMeta.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								runTask(taskUpdateStage, progress);
								Platform.runLater(() -> {
									XdmService.generateMeta(listView.getItems());
									final Path path = Paths.get(FILENAME + "\\" + "nouveauDoc.xml");
									final File initialFile = path.toFile();
									InputStream targetStream;
									try {
										targetStream = new FileInputStream(initialFile);
										textArea.setText(readFileContents(targetStream));
										final Alert alert = new Alert(AlertType.INFORMATION);
										final DialogPane dialogPane = alert.getDialogPane();
										dialogPane.getStylesheets()
												.add(getClass().getResource(Constant.CSS).toExternalForm());
										dialogPane.getStyleClass().add(Constant.DIALOG);
										dialogPane.setMinHeight(130);
										dialogPane.setMaxHeight(130);
										dialogPane.setPrefHeight(130);
										alert.setContentText("Traitement terminé avec succès");
										alert.setHeaderText(null);
										alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
										alert.showAndWait();
									} catch (final FileNotFoundException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
								});
							}
						});

						final Button buttonSave = new Button();
						final ImageView viewSave = new ImageView(Constant.SAVEFILE);
						buttonSave.setGraphic(viewSave);
						buttonSave.setStyle(Constant.STYLE1);
						buttonSave.setPrefSize(70, 50);
						buttonSave.setMinSize(70, 50);
						buttonSave.setMaxSize(70, 50);
						final Tooltip tooltipSave = new Tooltip("Sauvgarder le métadata");
						buttonSave.setTooltip(tooltipSave);
						buttonSave.setPadding(new Insets(0, 0, 0, 10));

						buttonSave.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								Platform.runLater(() -> {
									final FileChooser fileChooserTwo = new FileChooser();
									final FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
											"XML files (*.xml)", "*.xml");
									fileChooserTwo.getExtensionFilters().add(extFilter);
									final File file = fileChooserTwo.showSaveDialog(stageTwo);
									if (file != null) {
										saveTextToFile(textArea.getText(), file);
									}
								});
							}
						});

						final Button buttonGen = new Button();
						final ImageView viewGen = new ImageView(Constant.ZIPFILE);
						buttonGen.setGraphic(viewGen);
						buttonGen.setStyle(Constant.STYLE1);
						buttonGen.setPrefSize(70, 50);
						buttonGen.setMinSize(70, 50);
						buttonGen.setMaxSize(70, 50);
						final Tooltip tooltipGen = new Tooltip("Génerer le fichier IHE_XDM.ZIP complet");
						buttonGen.setTooltip(tooltipGen);
						buttonGen.setPadding(new Insets(0, 0, 0, 10));

						buttonGen.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								Platform.runLater(() -> {
									DirectoryChooser directoryChooser = new DirectoryChooser();
									directoryChooser.setInitialDirectory(new File("C:\\"));
									File selectedDirectory = directoryChooser.showDialog(stageTwo);
									if (names != null && !names.isEmpty()) {
										IheXdmService.generateIheXdmZip(names, selectedDirectory.getAbsolutePath());
										final Alert alert = new Alert(AlertType.INFORMATION);
										final DialogPane dialogPane = alert.getDialogPane();
										dialogPane.getStylesheets()
												.add(getClass().getResource(Constant.CSS).toExternalForm());
										dialogPane.getStyleClass().add(Constant.DIALOG);
										dialogPane.setMinHeight(130);
										dialogPane.setMaxHeight(130);
										dialogPane.setPrefHeight(130);
										alert.setContentText("Traitement terminé avec succès");
										alert.setHeaderText(null);
										alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
										alert.showAndWait();
									}
								});
							}
						});

						final Button buttonVal = new Button();
						final ImageView viewVal = new ImageView(Constant.VALIDATE);
						buttonVal.setGraphic(viewVal);
						buttonVal.setStyle(Constant.STYLE1);
						buttonVal.setPrefSize(70, 50);
						buttonVal.setMinSize(70, 50);
						buttonVal.setMaxSize(70, 50);
						final Tooltip tooltipVal = new Tooltip("Valider en ligne le fichier META");
						buttonVal.setTooltip(tooltipVal);
						buttonVal.setPadding(new Insets(0, 0, 0, 10));

						buttonVal.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								Platform.runLater(() -> {
									try {
										if (!textArea.getText().isEmpty()) {
											final Path temp = Files.createTempFile("META", ".xml");
											final BufferedWriter writer = new BufferedWriter(
													new FileWriter(temp.toFile()));
											writer.write(textArea.getText());
											writer.close();
											ValidationService.validateMeta(temp.toFile(), Constant.MODEL,
													Constant.ASIPXDM, Constant.URLVALIDATION);
											final Alert alert = new Alert(AlertType.INFORMATION);
											final DialogPane dialogPane = alert.getDialogPane();
											dialogPane.getStylesheets()
													.add(getClass().getResource(Constant.CSS).toExternalForm());
											dialogPane.getStyleClass().add(Constant.DIALOG);
											dialogPane.setMinHeight(130);
											dialogPane.setMaxHeight(130);
											dialogPane.setPrefHeight(130);
											alert.setContentText("Traitement terminé avec succès");
											alert.setHeaderText(null);
											alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
											alert.showAndWait();
											temp.toFile().delete();
										} else {
											final Alert alert = new Alert(AlertType.ERROR);
											final DialogPane dialogPane = alert.getDialogPane();
											dialogPane.getStylesheets()
													.add(getClass().getResource(Constant.CSS).toExternalForm());
											dialogPane.getStyleClass().add(Constant.DIALOG);
											dialogPane.setMinHeight(130);
											dialogPane.setMaxHeight(130);
											dialogPane.setPrefHeight(130);
											alert.setContentText("Merci de renseigner le METADATA d'abord");
											alert.setHeaderText(null);
											alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
											alert.showAndWait();
										}
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
								});
							}
						});

						final Button buttonCrossVal = new Button();
						final ImageView viewCrossVal = new ImageView(Constant.CROSS);
						buttonCrossVal.setGraphic(viewCrossVal);
						buttonCrossVal.setStyle(Constant.STYLE1);
						buttonCrossVal.setPrefSize(70, 50);
						buttonCrossVal.setMinSize(70, 50);
						buttonCrossVal.setMaxSize(70, 50);
						final Tooltip tooltipCrossVal = new Tooltip("Cross-Valider les fichiers CDA et META");
						buttonCrossVal.setTooltip(tooltipCrossVal);
						buttonCrossVal.setPadding(new Insets(0, 0, 0, 10));

						buttonCrossVal.setOnAction(new EventHandler<>() {
							@Override
							public void handle(final ActionEvent event) {
								Platform.runLater(() -> {
									try {
										if (!textArea.getText().isEmpty()) {
											final Path temp = Files.createTempFile("CROSS", ".xml");
											final BufferedWriter writer = new BufferedWriter(
													new FileWriter(temp.toFile()));
											writer.write(textArea.getText());
											writer.close();
											CrossValidationService.crossValidate(new File(names.get(0)), temp.toFile(),
													Constant.URLVALIDATION);
											final Alert alert = new Alert(AlertType.INFORMATION);
											final DialogPane dialogPane = alert.getDialogPane();
											dialogPane.getStylesheets()
													.add(getClass().getResource(Constant.CSS).toExternalForm());
											dialogPane.getStyleClass().add(Constant.DIALOG);
											dialogPane.setMinHeight(130);
											dialogPane.setMaxHeight(130);
											dialogPane.setPrefHeight(130);
											alert.setContentText("Traitement terminé avec succès");
											alert.setHeaderText(null);
											alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
											alert.showAndWait();
											temp.toFile().delete();
										} else {
											final Alert alert = new Alert(AlertType.ERROR);
											final DialogPane dialogPane = alert.getDialogPane();
											dialogPane.getStylesheets()
													.add(getClass().getResource(Constant.CSS).toExternalForm());
											dialogPane.getStyleClass().add(Constant.DIALOG);
											dialogPane.setMinHeight(130);
											dialogPane.setMaxHeight(130);
											dialogPane.setPrefHeight(130);
											alert.setContentText("Merci de renseigner le METADATA d'abord");
											alert.setHeaderText(null);
											alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
											alert.showAndWait();
										}
									} catch (final IOException e) {
										if (LOG.isInfoEnabled()) {
											final String error = e.getMessage();
											LOG.error(error);
										}
									}
								});
							}
						});

						final Button buttonVerif = new Button();
						final ImageView viewVerif = new ImageView(Constant.CHECK);
						buttonVerif.setGraphic(viewVerif);
						buttonVerif.setStyle(Constant.STYLE1);
						buttonVerif.setPrefSize(70, 50);
						buttonVerif.setMinSize(70, 50);
						buttonVerif.setMaxSize(70, 50);
						final Tooltip tooltipVerif = new Tooltip("Vérifier la présence d'erreurs dans le métadata");
						buttonVerif.setTooltip(tooltipVerif);
						buttonVerif.setPadding(new Insets(0, 0, 0, 10));

						final Button buttonParc = new Button();
						final ImageView viewParc = new ImageView(Constant.ERROR);
						buttonParc.setGraphic(viewParc);
						buttonParc.setStyle(Constant.STYLE1);
						buttonParc.setPrefSize(70, 50);
						buttonParc.setMinSize(70, 50);
						buttonParc.setMaxSize(70, 50);
						final Tooltip tooltipParc = new Tooltip("Parcourir les erreurs de génération");
						buttonParc.setTooltip(tooltipParc);
						buttonParc.setPadding(new Insets(0, 0, 0, 10));

						final Region spacer3 = new Region();
						spacer3.setMaxWidth(10);
						HBox.setHgrow(spacer3, Priority.ALWAYS);

						final Region spacer4 = new Region();
						spacer4.setMaxWidth(10);
						HBox.setHgrow(spacer4, Priority.ALWAYS);

						final Region spacer5 = new Region();
						spacer5.setMaxWidth(10);
						HBox.setHgrow(spacer5, Priority.ALWAYS);

						final Region spacer6 = new Region();
						spacer6.setMaxWidth(10);
						HBox.setHgrow(spacer6, Priority.ALWAYS);

						final Region spacer7 = new Region();
						spacer7.setMaxWidth(30);
						HBox.setHgrow(spacer7, Priority.ALWAYS);

						final Region spacer8 = new Region();
						spacer8.setMaxWidth(10);
						HBox.setHgrow(spacer8, Priority.ALWAYS);

						final HBox hbox4 = new HBox();
						hbox4.getChildren().addAll(buttonMeta, spacer3, buttonSave, spacer4, buttonGen, spacer5,
								buttonVal, spacer6, buttonCrossVal, spacer7, buttonVerif, spacer8, buttonParc);
						hbox4.setPadding(new Insets(20, 0, 0, 20));

						final SplitPane splitPane = new SplitPane();
						splitPane.setStyle("-fx-box-border: 0px;");
						splitPane.setOrientation(Orientation.HORIZONTAL);
						splitPane.setDividerPositions(0.5f, 0.5f);
						splitPane.setPadding(new Insets(20, 0, 0, 20));

						textArea.setPrefHeight(Integer.MAX_VALUE);
						textArea.setPrefWidth(Integer.MAX_VALUE);
						final HBox box = new HBox();
						final Label labelRapport = new Label("Aucun rapport à afficher");
						labelRapport.setStyle("-fx-font-weight: bold");
						box.getChildren().add(labelRapport);
						final HBox box1 = new HBox();
						box1.getChildren().add(textArea);

						LocalUtility.getItemsSplit(splitPane).addAll(box1, box);

						hbox.getChildren().addAll(listView, vbox);
						root.getChildren().addAll(label, hbox, hbox1, hbox2, hbox3, hbox4, splitPane);

						final Scene scene = new Scene(root, Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
						stageTwo.setMaximized(true);
						stageTwo.setScene(scene);
						stageTwo.setTitle("metaGenerator");
						stageTwo.show();

					});
				} else {

					final Alert alert = new Alert(AlertType.ERROR);
					final DialogPane dialogPane = alert.getDialogPane();
					dialogPane.getStylesheets().add(getClass().getResource(Constant.CSS).toExternalForm());
					dialogPane.getStyleClass().add(Constant.DIALOG);
					dialogPane.setMinHeight(130);
					dialogPane.setMaxHeight(130);
					dialogPane.setPrefHeight(130);
					alert.setContentText("Merci de renseigner le fichier CDA");
					alert.setHeaderText(null);
					alert.getDialogPane().lookupButton(ButtonType.OK).setVisible(true);
					alert.showAndWait();
				}
			}
		});

		final Button button42 = new Button();
		final ImageView view42 = new ImageView(Constant.ZIPXDM);
		button42.setGraphic(view42);
		button42.setStyle(Constant.STYLE1);
		button42.setPrefSize(70, 40);
		button42.setMinSize(70, 40);
		button42.setMaxSize(70, 40);
		final Tooltip tooltip42 = new Tooltip("Générer des archives XDM pour l'ensemble des CDA d'un répertoire");
		button42.setTooltip(tooltip42);

		final Button button43 = new Button();
		final ImageView view43 = new ImageView(Constant.SEARCH);
		button43.setGraphic(view43);
		button43.setStyle(Constant.STYLE1);
		button43.setPrefSize(70, 40);
		button43.setMinSize(70, 40);
		button43.setMaxSize(70, 40);
		final Tooltip tooltip43 = new Tooltip("Accéder au laboratoire cd cross validation unitaire");
		button43.setTooltip(tooltip43);

		hbox41.setStyle(Constant.STYLE17);
		hbox41.getChildren().addAll(button41, button42, button43);

		final Button button44 = new Button();
		final ImageView view44 = new ImageView(Constant.UUID);
		button44.setGraphic(view44);
		button44.setStyle(Constant.STYLE1);
		button44.setPrefSize(70, 40);
		button44.setMinSize(70, 40);
		button44.setMaxSize(70, 40);
		final Tooltip tooltip44 = new Tooltip("Correction des UUID des éléments <id>");
		button44.setTooltip(tooltip44);

		final Button button45 = new Button();
		final ImageView view45 = new ImageView(Constant.HACH);
		button45.setGraphic(view45);
		button45.setStyle(Constant.STYLE1);
		button45.setPrefSize(70, 40);
		button45.setMinSize(70, 40);
		button45.setMaxSize(70, 40);
		final Tooltip tooltip45 = new Tooltip("Calcul du hash avec canonisation préalable");
		button45.setTooltip(tooltip45);

		final Button button46 = new Button();
		final ImageView view46 = new ImageView(Constant.BIO);
		button46.setGraphic(view46);
		button46.setStyle(Constant.STYLE1);
		button46.setPrefSize(70, 40);
		button46.setMinSize(70, 40);
		button46.setMaxSize(70, 40);
		final Tooltip tooltip46 = new Tooltip("Contrôle des codes BIO LOINC du CDA");
		button46.setTooltip(tooltip46);

		final Button button47 = new Button();
		final ImageView view47 = new ImageView(Constant.BOM);
		button47.setGraphic(view47);
		button47.setStyle(Constant.STYLE1);
		button47.setPrefSize(70, 40);
		button47.setMinSize(70, 40);
		button47.setMaxSize(70, 40);
		final Tooltip tooltip47 = new Tooltip("Supprimer l'encodage BOM de tous les fichiers XML d'un répertoire");
		button47.setTooltip(tooltip47);

		final Button button48 = new Button();
		final ImageView view48 = new ImageView(Constant.ENGINE);
		button48.setGraphic(view48);
		button48.setStyle(Constant.STYLE1);
		button48.setPrefSize(70, 40);
		button48.setMinSize(70, 40);
		button48.setMaxSize(70, 40);
		final Tooltip tooltip48 = new Tooltip("Module de recherche Xpath dans un répertoire de CDA");
		button48.setTooltip(tooltip48);

		final Button button49 = new Button();
		final ImageView view49 = new ImageView(Constant.ANUUL);
		button49.setGraphic(view49);
		button49.setStyle(Constant.STYLE1);
		button49.setPrefSize(70, 40);
		button49.setMinSize(70, 40);
		button49.setMaxSize(70, 40);
		final Tooltip tooltip49 = new Tooltip("Contrôles des erreurs courantes présentes dans le CDA");
		button49.setTooltip(tooltip49);

		final Button button50 = new Button();
		final ImageView view50 = new ImageView(Constant.AUCUN);
		button50.setGraphic(view50);
		button50.setStyle(Constant.STYLE1);
		button50.setPrefSize(70, 40);
		button50.setMinSize(70, 40);
		button50.setMaxSize(70, 40);
		final Tooltip tooltip50 = new Tooltip(
				"Contrôles des erreurs courantes présentes dans tous les CDA d'un répertoire");
		button50.setTooltip(tooltip50);

		final Button button51 = new Button();
		final ImageView view51 = new ImageView(Constant.APPROVE);
		button51.setGraphic(view51);
		button51.setStyle(Constant.STYLE1);
		button51.setPrefSize(70, 40);
		button51.setMinSize(70, 40);
		button51.setMaxSize(70, 40);
		final Tooltip tooltip51 = new Tooltip("Validation par API des fichiers CDA et XDM");
		button51.setTooltip(tooltip51);

		final Button button52 = new Button();
		final ImageView view52 = new ImageView(Constant.FHIR);
		button52.setGraphic(view52);
		button52.setStyle(Constant.STYLE1);
		button52.setPrefSize(70, 40);
		button52.setMinSize(70, 40);
		button52.setMaxSize(70, 40);
		final Tooltip tooltip52 = new Tooltip("Accès au module FHIR");
		button52.setTooltip(tooltip52);

		final Button button53 = new Button();
		final ImageView view53 = new ImageView(Constant.MODIFY);
		button53.setGraphic(view53);
		button53.setStyle(Constant.STYLE1);
		button53.setPrefSize(70, 40);
		button53.setMinSize(70, 40);
		button53.setMaxSize(70, 40);

		final Button button54 = new Button();
		final ImageView view54 = new ImageView(Constant.SCH);
		button54.setGraphic(view54);
		button54.setStyle(Constant.STYLE1);
		button54.setPrefSize(70, 40);
		button54.setMinSize(70, 40);
		button54.setMaxSize(70, 40);

		final Button button55 = new Button();
		final ImageView view55 = new ImageView(Constant.PARAM1);
		button55.setGraphic(view55);
		button55.setStyle(Constant.STYLE1);
		button55.setPrefSize(70, 40);
		button55.setMinSize(70, 40);
		button55.setMaxSize(70, 40);

		final Button button56 = new Button();
		final ImageView view56 = new ImageView(Constant.PARAM2);
		button56.setGraphic(view56);
		button56.setStyle(Constant.STYLE1);
		button56.setPrefSize(70, 40);
		button56.setMinSize(70, 40);
		button56.setMaxSize(70, 40);

		final Button button57 = new Button();
		button57.setGraphic(view57);
		button57.setStyle(Constant.STYLE1);
		button57.setPrefSize(70, 40);
		button57.setMinSize(70, 40);
		button57.setMaxSize(70, 40);
		final Tooltip tooltip57 = new Tooltip("Afficher la documentation de l'application");
		button57.setTooltip(tooltip57);

		final File file = new File(url.getFile());
		webEngine.load(file.toURI().toString());

		browserEngine.getChildrenUnmodifiable().addListener(new ListChangeListener<>() {
			@Override
			public void onChanged(Change<? extends Node> change) {
				final Set<Node> nodes = browserEngine.lookupAll(".scroll-bar");
				for (final Node node : nodes) {
					if (node instanceof ScrollBar) {
						final ScrollBar sbuilder = (ScrollBar) node;
						if (sbuilder.getOrientation().equals(Orientation.HORIZONTAL)) {
							sbuilder.setVisible(false);
						}
					}
				}
			}
		});

		button57.setOnAction(new EventHandler<>() {
			@Override
			public void handle(final ActionEvent event) {
				if (view57.getImage().getUrl().endsWith(Constant.POINT)) {
					final URL url = this.getClass().getClassLoader().getResource(Constant.HTMLFILE);
					final File file = new File(url.getFile());
					webEngine.load(file.toURI().toString());
					view57 = new ImageView(Constant.CLOSE);
					button57.setGraphic(view57);
				} else if (view57.getImage().getUrl().endsWith(Constant.CLOSE)) {
					final File file = new File(url.getFile());
					webEngine.load(file.toURI().toString());
					view57 = new ImageView(Constant.POINT);
					button57.setGraphic(view57);
				}
			}
		});

		hbox42.setStyle(Constant.STYLE17);
		hbox42.getChildren().addAll(button44, button45, button46, button47);

		final Label label41 = new Label("Fonctions XDM");
		final VBox vbox41 = new VBox();
		vbox41.getChildren().addAll(label41, hbox41);
		hbox4.getChildren().addAll(vbox41);

		final Label label42 = new Label("Contrôles du CDA");
		final VBox vbox42 = new VBox();
		vbox42.getChildren().addAll(label42, hbox42);
		hbox5.getChildren().addAll(vbox42);

		hbox43.setStyle(Constant.STYLE17);
		hbox43.getChildren().addAll(button48);

		final Label label43 = new Label("Recherche Xpath");
		final VBox vbox43 = new VBox();
		vbox43.getChildren().addAll(label43, hbox43);
		hbox6.getChildren().addAll(vbox43);

		final CheckBox checkBox = new CheckBox("Corriger \n les erreurs");
		hbox44.setStyle(Constant.STYLE17);
		hbox44.getChildren().addAll(button49, button50, checkBox);

		final Label label44 = new Label("Erreurs courantes");
		final VBox vbox44 = new VBox();
		vbox44.getChildren().addAll(label44, hbox44);
		hbox7.getChildren().addAll(vbox44);

		final Label label45 = new Label("Validation API");
		hbox45.setStyle(Constant.STYLE17);
		hbox45.getChildren().addAll(button51);

		final VBox vbox45 = new VBox();
		vbox45.getChildren().addAll(label45, hbox45);
		hbox8.getChildren().addAll(vbox45);

		final Label label46 = new Label("FHIR");
		hbox46.setStyle(Constant.STYLE17);
		hbox46.getChildren().addAll(button52);

		final VBox vbox46 = new VBox();
		vbox46.getChildren().addAll(label46, hbox46);
		hbox9.getChildren().addAll(vbox46);

		final Label label47 = new Label("Art Decor");
		hbox47.setStyle(Constant.STYLE17);
		hbox47.getChildren().addAll(button53, button54);

		final VBox vbox47 = new VBox();
		vbox47.getChildren().addAll(label47, hbox47);
		hbox10.getChildren().addAll(vbox47);

		final Label label48 = new Label("Paramétrage/Documentation");
		hbox48.setStyle(Constant.STYLE17);
		hbox48.getChildren().addAll(button55, button56, button57);

		final VBox vbox48 = new VBox();
		vbox48.getChildren().addAll(label48, hbox48);
		hbox11.getChildren().addAll(vbox48);

		final Region spacer5 = new Region();
		spacer5.setMaxWidth(10);
		HBox.setHgrow(spacer5, Priority.ALWAYS);

		final Region spacer6 = new Region();
		spacer6.setMaxWidth(10);
		HBox.setHgrow(spacer6, Priority.ALWAYS);

		final Region spacer7 = new Region();
		spacer7.setMaxWidth(10);
		HBox.setHgrow(spacer7, Priority.ALWAYS);

		final Region spacer8 = new Region();
		spacer8.setMaxWidth(10);
		HBox.setHgrow(spacer8, Priority.ALWAYS);

		final Region spacer9 = new Region();
		spacer9.setMaxWidth(10);
		HBox.setHgrow(spacer9, Priority.ALWAYS);

		final Region spacer10 = new Region();
		spacer10.setMaxWidth(10);
		HBox.setHgrow(spacer10, Priority.ALWAYS);

		final Region spacer11 = new Region();
		spacer11.setMaxWidth(10);
		HBox.setHgrow(spacer11, Priority.ALWAYS);

		final HBox hbAll = new HBox();
		hbAll.getChildren().addAll(hbox4, spacer5, hbox5, spacer6, hbox6, spacer7, hbox7, spacer8, hbox8, spacer9,
				hbox9, spacer10, hbox10, spacer11, hbox11);

		final VBox vBoxAll = new VBox(5);
		vBoxAll.getChildren().addAll(labelCda, hbox1, labelMeta, hbox2, hbAll);

		final SplitPane splitPane = new SplitPane();
		splitPane.setStyle("-fx-box-border: 0px;");
		splitPane.setOrientation(Orientation.HORIZONTAL);
		splitPane.setDividerPositions(0.01f, 0.99f);
		LocalUtility.getItemsSplit(splitPane).addAll(vBoxImg, vBoxAll);

		final ScrollPane scrollPane = new ScrollPane();
		scrollPane.setFitToHeight(true);
		scrollPane.setFitToWidth(true);
		scrollPane.setContent(browserEngine);
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
		final BorderPane borderPane = new BorderPane();
		borderPane.setCenter(textAreaConsole);
		vBox.getChildren().addAll(splitPane, borderPane, scrollPane);

		final VBox hBox2 = new VBox();
		hBox2.setMinSize(vBox.getPrefWidth(), vBox.getPrefHeight());
		final ObservableList<Node> listHB = LocalUtility.getChildrenNode(hBox2);
		listHB.addAll(vBox);
		// Creating a scene object
		final Scene scene = new Scene(hBox2, Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
		// Setting title to the Stage
		stage.setTitle("InteropStudio2024");
		// Adding scene to the stage
		stage.setScene(scene);
		stage.setMaximized(true);
		stage.show();
		browserEngine.requestFocus();

		stage.setOnCloseRequest(new EventHandler<>() {
			@Override
			public void handle(final WindowEvent event) {
				stage.close();
				Platform.exit();
			}
		});
	}

	/**
	 * saveTextToFile
	 * 
	 * @param content
	 * @param file
	 */
	private void saveTextToFile(final String content, final File file) {
		try {
			PrintWriter writer;
			writer = new PrintWriter(file);
			writer.println(content);
			writer.close();
		} catch (final IOException e) {
			if (LOG.isInfoEnabled()) {
				final String error = e.getMessage();
				LOG.error(error);
			}
		}
	}

	/**
	 * readFileContents
	 * 
	 * @param selectedFile
	 * @throws IOException
	 */
	public static String readFileContents(final InputStream file) throws IOException {
		String singleString;
		try (BufferedReader bReader = new BufferedReader(new InputStreamReader(file))) {
			final StringBuilder sbuilder = new StringBuilder();
			String line = bReader.readLine();
			while (line != null) {
				sbuilder.append(line).append(Constant.RETOURCHARIOT);
				line = bReader.readLine();
			}
			singleString = sbuilder.toString();
		}
		return singleString;
	}
}