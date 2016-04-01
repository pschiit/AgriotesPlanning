package fr.agriotes.planning.controllers;

import fr.agriotes.planning.models.Date;
import fr.agriotes.planning.models.Module;
import fr.agriotes.planning.models.Seance;
import fr.agriotes.planning.models.CalendarCell;
import fr.agriotes.planning.models.Formateur;
import fr.agriotes.planning.models.Session;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import java.util.List;
import fr.agriotes.planning.services.CalendrierControllerServices;
import java.util.HashMap;
import java.util.Map;

public class CalendrierAnnuelController {

    private CalendrierControllerServices calendrierControllerServices;
    private List<Seance> lesSeances;
    private Session sessionSelectionnee;
    private Module moduleSelectionne;
    private Formateur formateurSelectionne;
    private Map<Date, CalendarCell> lesCalendarCells = new HashMap<>();

    public CalendrierControllerServices getCalendrierControllerService() {
        return calendrierControllerServices;
    }

    public void setCalendrierControllerService(CalendrierControllerServices calendrierControllerService) {
        this.calendrierControllerServices = calendrierControllerService;
    }

    public List<Seance> getLesSeances() {
        return lesSeances;
    }

    public void setLesSeances(List<Seance> lesSeances) {
        this.lesSeances = lesSeances;
    }

    public Session getSessionSelectionnee() {
        return sessionSelectionnee;
    }

    public void setSessionSelectionnee(Session sessionSelectionnee) {
        System.out.println(sessionSelectionnee);
        this.sessionSelectionnee = sessionSelectionnee;
        setModuleSelectionne(null);
        setFormateurSelectionne(null);
        initialize();
    }

    public Module getModuleSelectionne() {
        return moduleSelectionne;
    }

    public void setModuleSelectionne(Module moduleSelectionne) {
        System.out.println(moduleSelectionne);
        this.moduleSelectionne = moduleSelectionne;
    }

    public Formateur getFormateurSelectionne() {
        return formateurSelectionne;
    }

    public void setFormateurSelectionne(Formateur formateurSelectionne) {
        System.out.println(formateurSelectionne);
        this.formateurSelectionne = formateurSelectionne;
    }

    public Map<Date, CalendarCell> getLesCalendarCells() {
        return lesCalendarCells;
    }

    public void setLesCalendarCells(Map<Date, CalendarCell> lesCalendarCells) {
        this.lesCalendarCells = lesCalendarCells;
    }

    @FXML
    private GridPane calendrierGridPane;
    @FXML
    private Label sessionTitle;

    @FXML
    public void initialize() {
        paint();
    }

    private void paint() {
        System.out.println("Calendrier annuel loading");
        calendrierGridPane.getChildren().clear();
        if (sessionSelectionnee == null) {
            sessionTitle.setText("Veuillez choisir une session.");
        } else {
            sessionTitle.setText(sessionSelectionnee.toString());
            int nbMois = sessionSelectionnee.getNombreDeMois();
            int moisDebut = sessionSelectionnee.getDateDebut().getMois();
            for (int i = 0; i < nbMois; i++) {
                int mois = (moisDebut + i - 1) % 12 + 1;
                Label headerMois = new Label(Date.MOIS.values()[mois].toString());
                headerMois.setMaxWidth(80.0);
                headerMois.setMaxHeight(25.0);
                headerMois.getStyleClass().add("calendrier-cell");
                calendrierGridPane.add(headerMois, i + 2, 1);
                for (int j = 0; j < 31; j++) {
                    int jour = j + 1;
                    int annee = moisDebut + i < 12 ? sessionSelectionnee.getDateDebut().getAnnee() : sessionSelectionnee.getDateFin().getAnnee();
                    Date date = new Date(annee, mois, jour);
                    //Inactive cell
                    if ((i == 0 && jour < sessionSelectionnee.getDateDebut().getJour())) {//Jour avant le début{
                        calendrierGridPane.add(inactiveCell(date), i + 2, j + 2);
                    } else if ((i == nbMois - 1 && jour > sessionSelectionnee.getDateFin().getJour())) {//Jour après la fin
                        calendrierGridPane.add(inactiveCell(date), i + 2, j + 2);
                    } else if (mois == 2 && jour > 28 && !(jour == 29 && (moisDebut + i < 12 && sessionSelectionnee.getDateDebut().isBisextile()) || (moisDebut + i > 12 && sessionSelectionnee.getDateFin().isBisextile()))) {//Fevrier
                        //calendrierGridPane.add(inactiveCell(null), i + 2, j + 2);
                    } else if (j == 30 && (mois == 4 || mois == 6 || mois == 9 || mois == 11)) {//Mois de 30 jours
                        //calendrierGridPane.add(inactiveCell(null), i + 2, j + 2);
                    } //Empty cell
                    else {
                        boolean done = false;
                        //Week-end
                        if (date.getDay() == 0 || date.getDay() == 6) {
                            calendrierGridPane.add(inactiveCell(date), i + 2, j + 2);
                            done = true;
                        } //Jour férié
                        else if (date.isFerie()) {
                            calendrierGridPane.add(inactiveCell(date), i + 2, j + 2);
                            done = true;
                        } else if (lesSeances != null) {
                            for (Seance uneSeance : lesSeances) {

                                Date dateSeance = uneSeance.getDate();
                                if (dateSeance.getAnnee() == annee && dateSeance.getMois() == mois && dateSeance.getJour() == j + 1) {
                                    calendrierGridPane.add(seanceCell(uneSeance), i + 2, j + 2);
                                    done = true;
                                    break;
                                }
                            }
                        }
                        if (!done) {
                            calendrierGridPane.add(emptyCell(date), i + 2, j + 2);
                        }
                    }
                }
            }
        }
    }

    private HBox inactiveCell(Date date) {
        HBox cell = new HBox();
        Label labelNumero = new Label(String.valueOf(date.getJour()));
        Label labelJour = new Label(Date.JOUR.values()[date.getDay()].toString().charAt(0) + "");
        Label labelSeance = new Label();
        labelNumero.setMinWidth(15);
        labelJour.setMinWidth(15);
        labelSeance.setMinWidth(40);
        labelNumero.getStyleClass().add("jour-cell");
        labelJour.getStyleClass().add("jour-cell");
        cell.getChildren().addAll(labelNumero, labelJour, labelSeance);
        cell.getStyleClass().addAll("calendrier-cell", "inactive-cell");
        return cell;
    }

    private CalendarCell emptyCell(final Date date) {
        final CalendarCell cell = new CalendarCell(date);
        lesCalendarCells.put(date, cell);
        cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
            //Selectionne la Session à planifier
            @Override
            public void handle(MouseEvent event) {
                if (cell.getSeance() == null) {
                    Seance nouvelleSeance = calendrierControllerServices.addSeance(new Seance(0, sessionSelectionnee, moduleSelectionne, formateurSelectionne, date));
                    if (nouvelleSeance != null) {
                        cell.setSeance(nouvelleSeance);
                        cell.setEvent(calendrierControllerServices);
                    }
                }
            }
        });
        return cell;
    }

    private CalendarCell seanceCell(final Seance seance) {
        CalendarCell cell = new CalendarCell(seance);
        lesCalendarCells.put(seance.getDate(), cell);
        cell.setEvent(calendrierControllerServices);
        return cell;
    }

    public void editCell(Seance seance) {
        CalendarCell cell = lesCalendarCells.get(seance.getDate());
        cell.setSeance(seance);
    }

    public void removeCell(Date date) {
        final CalendarCell cell = lesCalendarCells.get(date);
        cell.setSeance(null);
        cell.setOnMouseClicked(new EventHandler<MouseEvent>() {
            //Reautorise la plannification pour cette cell
            @Override
            public void handle(MouseEvent event) {
                if (cell.getSeance() == null) {
                    Seance nouvelleSeance = calendrierControllerServices.addSeance(new Seance(0, sessionSelectionnee, moduleSelectionne, formateurSelectionne, cell.getDate()));
                    if (nouvelleSeance != null) {
                        cell.setSeance(nouvelleSeance);
                        cell.setEvent(calendrierControllerServices);
                    }
                }
            }
        });
    }
}
