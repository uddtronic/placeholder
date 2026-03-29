package se.uddtronic.placeholder.ingestion.ui;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import se.uddtronic.placeholder.ingestion.DataStorageService;
import se.uddtronic.placeholder.ingestion.StoredData;
import se.uddtronic.placeholder.ui.MainLayout;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Requests")
@Menu(order = 1, icon = "vaadin:database", title = "Requests")
@JavaScript("context://prism.js")
@StyleSheet("context://prism.css")
public class DataListView extends VerticalLayout {
    private final DataStorageService dataStorageService;
    private final List<StoredData> gridItems = new ArrayList<>();
    private ListDataProvider<StoredData> dataProvider = new ListDataProvider<>(gridItems);
    private Registration listenerRegistration;

    public DataListView(DataStorageService dataStorageService) {
        this.dataStorageService = dataStorageService;
        gridItems.addAll(dataStorageService.getAll());

        setSizeFull();

        Grid<StoredData> grid = new Grid<>();
        grid.setDataProvider(dataProvider);
        var receivedColumn = grid.addColumn(StoredData::getFormattedCreatedAt)
            .setComparator(StoredData::getCreatedAt)
            .setHeader("Received")
            .setWidth("20rem")
            .setFlexGrow(0)
            .setSortable(true);
        grid.sort(GridSortOrder.desc(receivedColumn).build());
        grid.addColumn(StoredData::getMethod)
            .setHeader("Method")
            .setWidth("10rem")
            .setFlexGrow(0)
            .setSortable(true);
        grid.addColumn(StoredData::getPath)
            .setHeader("Path")
            .setAutoWidth(true)
            .setSortable(true);
        grid.addComponentColumn(this::createStatusBadge)
            .setHeader("Status")
            .setWidth("10rem")
            .setFlexGrow(0)
            .setSortable(true);

        grid.addComponentColumn(storedData -> {
            Button button = new Button("Show Details");
            button.addClickListener(click -> openDetailDialog(storedData));
            return button;
        })
            .setHeader("Details")
            .setWidth("20rem")
            .setFlexGrow(0);

        var searchField = new TextField();
        searchField.setPlaceholder("Search");
        searchField.setWidthFull();
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setValueChangeMode(com.vaadin.flow.data.value.ValueChangeMode.EAGER);
        searchField.addValueChangeListener(e -> {
            String searchTerm = e.getValue();
            dataProvider.setFilter(item -> {
                if (searchTerm == null || searchTerm.isBlank()) return true;
                String lowerCaseTerm = searchTerm.toLowerCase();
                return item.getPath().toLowerCase().contains(lowerCaseTerm) ||
                       item.getMethod().toLowerCase().contains(lowerCaseTerm) ||
                       item.getData().toLowerCase().contains(lowerCaseTerm) ||
                       formatQueryParameters(item).toLowerCase().contains(lowerCaseTerm);
            });
        });

        var clearButton = new Button("Clear requests");
        clearButton.addClickListener(e -> {
            var confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Clear all requests?");
            confirmDialog.setText("Are you sure you want to clear all requests? This action cannot be undone.");
            confirmDialog.setConfirmButton("Clear", ev -> {
                dataStorageService.clear();
            });
            confirmDialog.setCancelButton("Cancel", ev -> confirmDialog.close());
            confirmDialog.open();
        });
        var topBar = new HorizontalLayout(searchField, clearButton);
        topBar.setWidthFull();
        topBar.setFlexGrow(1, searchField);

        add(topBar, grid);
        setFlexGrow(1, grid);
    }

    private Span createStatusBadge(StoredData item) {
        int statusCode = item.getStatus();
        Span badge = new Span(String.valueOf(statusCode));
        badge.getElement().getThemeList().add("badge");

        if (statusCode >= 200 && statusCode < 300) {
            badge.getElement().getThemeList().add("success");
        } else if (statusCode >= 300 && statusCode < 400) {
            badge.getElement().getThemeList().add("contrast-20");
        } else if (statusCode >= 400 && statusCode < 500) {
            badge.getElement().getThemeList().add("warning");
        } else if (statusCode >= 500 && statusCode < 600) {
            badge.getElement().getThemeList().add("error");
        } else {
            badge.getElement().getThemeList().add("contrast-30");
        }

        return badge;
    }

    private void openDetailDialog(StoredData storedData) {
        Dialog dialog = new Dialog();
        dialog.setWidth("80%");
        dialog.setHeight("80%");
        dialog.setHeaderTitle(String.format("%s %s - %s", storedData.getMethod(), storedData.getPath(),
                storedData.getFormattedCreatedAt()));

        Pre dataContent = new Pre(storedData.getData());
        dataContent.addClassNames(getLanguageClass(storedData.getContentType()));
        dataContent.getStyle()
            .set("min-width", "0")
            .set("overflow", "auto")
            .set("flex-grow", "1");
        dataContent.setVisible(false);

        Pre headersContent = new Pre(formatHeaders(storedData));
        headersContent.addClassNames("language-http");
        headersContent.getStyle()
            .set("min-width", "0")
            .set("overflow", "auto")
            .set("flex-grow", "1");
        headersContent.setVisible(false);

        Pre queryContent = new Pre(formatQueryParameters(storedData));
        queryContent.addClassNames("language-http");
        queryContent.getStyle()
            .set("min-width", "0")
            .set("overflow", "auto")
            .set("flex-grow", "1");
        queryContent.setVisible(false);

        Pre validationContent = new Pre();
        if (storedData.getValidationErrors() != null && !storedData.getValidationErrors().isEmpty()) {
            validationContent.setText(String.join("\n", storedData.getValidationErrors()));
        }
        validationContent.getStyle()
            .set("min-width", "0")
            .set("overflow", "auto")
            .set("flex-grow", "1")
            .set("color", "var(--lumo-error-text-color)");
        validationContent.setVisible(false);

        boolean firstVisible = true;
        Map<Tab, Component> tabToContentMap = new LinkedHashMap<>();
        if (storedData.getData() != null && !storedData.getData().isBlank()) {
            tabToContentMap.put(new Tab("Body"), dataContent);
            dataContent.setVisible(firstVisible);
            firstVisible = false;
        }
        if (storedData.getHeaders() != null && !storedData.getHeaders().isEmpty()) {
            tabToContentMap.put(new Tab("Headers"), headersContent);
            headersContent.setVisible(firstVisible);
            firstVisible = false;
        }
        if (storedData.getQueryParameters() != null && !storedData.getQueryParameters().isEmpty()) {
            tabToContentMap.put(new Tab("Query"), queryContent);
            queryContent.setVisible(firstVisible);
            firstVisible = false;
        }
        if (storedData.getValidationErrors() != null && !storedData.getValidationErrors().isEmpty()) {
            tabToContentMap.put(new Tab("Validation"), validationContent);
            validationContent.setVisible(firstVisible);
            firstVisible = false;
        }

        Tabs tabs = new Tabs(tabToContentMap.keySet().toArray(new Tab[0]));
        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        tabs.addSelectedChangeListener(event -> {
            tabToContentMap.values().forEach(content -> content.setVisible(false));
            Component selectedContent = tabToContentMap.get(event.getSelectedTab());
            if (selectedContent != null) {
                selectedContent.setVisible(true);
            }
        });

        Button copyButton = new Button(new Icon(VaadinIcon.COPY));
        copyButton.setTooltipText("Copy visible content");
        copyButton.addClickListener(e -> {
            String js = "const active = Array.from(document.querySelectorAll('pre')).find(p => p.hidden !== true);" +
                         "navigator.clipboard.writeText(active.innerText);";
            copyButton.getElement().executeJs(js);
            Notification.show("Copied!", 1000, Notification.Position.MIDDLE);
        });

        dialog.getHeader().add(copyButton);

        HorizontalLayout contentPanel = new HorizontalLayout(dataContent, headersContent, queryContent, validationContent);
        contentPanel.setSizeFull();
        contentPanel.setPadding(false);
        contentPanel.setSpacing(false);

        HorizontalLayout dialogLayout = new HorizontalLayout(tabs, contentPanel);
        dialogLayout.setSizeFull();
        dialogLayout.setFlexGrow(1, contentPanel);
        dialog.add(dialogLayout);

        Button closeButton = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(closeButton);

        dialog.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                dataContent.getElement().executeJs("Prism.highlightElement(this);");
                headersContent.getElement().executeJs("Prism.highlightElement(this);");
                queryContent.getElement().executeJs("Prism.highlightElement(this);");
            }
        });

        dialog.open();
    }
    private String getLanguageClass(String contentType) {
        if (contentType == null) {
            return "language-none";
        }
        if (contentType.contains("json")) {
            return "language-json";
        }
        return "language-none";
    }

    private String formatHeaders(StoredData storedData) {
        StringBuilder sb = new StringBuilder();
        if (storedData.getHeaders() != null) {
            storedData.getHeaders().forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));
        }
        return sb.toString();
    }

    private String formatQueryParameters(StoredData storedData) {
        StringBuilder sb = new StringBuilder();
        if (storedData.getQueryParameters() != null) {
            storedData.getQueryParameters().forEach((k, v) -> {
                for (String value : v) {
                    sb.append(k).append("=").append(value).append("\n");
                }
            });
        }
        return sb.toString();
    }

    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        listenerRegistration = dataStorageService.register(update -> {
            attachEvent.getUI().access(() -> {
                if (update.isClear()) {
                    gridItems.clear();
                } else {
                    gridItems.add(0, update.addedItem());
                    if (update.removedItem() != null) gridItems.remove(update.removedItem());
                }
                dataProvider.refreshAll();
            });
        });
    }

    @Override
    protected void onDetach(com.vaadin.flow.component.DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}
