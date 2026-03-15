package se.uddtronic.placeholder.mocks.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Pre;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.util.List;
import se.uddtronic.placeholder.mocks.MockData;
import se.uddtronic.placeholder.mocks.MocksService;
import se.uddtronic.placeholder.ui.MainLayout;

@PermitAll
@PageTitle("Mocks")
@Route(value = "mocks", layout = MainLayout.class)
@Menu(order = 2, icon = "vaadin:file-text", title = "Mocks")
@JavaScript("context://prism.js")
@StyleSheet("context://prism.css")
public class MocksListView extends VerticalLayout {

    public MocksListView(MocksService mocksService) {
        setSizeFull();

        List<MockData> mocks = mocksService.getMocks();
        ListDataProvider<MockData> dataProvider = new ListDataProvider<>(mocks);

        Grid<MockData> grid = new Grid<>();
        grid.setDataProvider(dataProvider);
        grid.addColumn(MockData::getName)
            .setHeader("Name")
            .setWidth("20rem")
            .setFlexGrow(0)
            .setSortable(true);
        grid.addColumn(MockData::getMethod)
            .setHeader("Method")
            .setWidth("10rem")
            .setFlexGrow(0)
            .setSortable(true);
        grid.addColumn(MockData::getPath)
            .setHeader("Path")
            .setAutoWidth(true)
            .setSortable(true);

        grid.addComponentColumn(mockData -> {
            Button button = new Button("Show Details");
            button.addClickListener(click -> openDetailDialog(mockData));
            return button;
        }).setHeader("Details").setWidth("12rem").setFlexGrow(0);

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
                       (item.getName() != null && item.getName().toLowerCase().contains(lowerCaseTerm));
            });
        });

        add(searchField, grid);
        setFlexGrow(1, grid);
    }

    private void openDetailDialog(MockData mockData) {
        Dialog dialog = new Dialog();
        dialog.setWidth("80%");
        dialog.setHeight("80%");
        if (mockData.getName() != null) {
            dialog.setHeaderTitle(String.format("%s (%s %s)", mockData.getName(), mockData.getMethod(), mockData.getPath()));
        } else {
            dialog.setHeaderTitle(String.format("%s %s", mockData.getMethod(), mockData.getPath()));
        }

        Span statusBadge = createStatusBadge(mockData.getResponse().getStatus());
        dialog.add(statusBadge);

        String responseJson = mockData.getResponse().getJson().toPrettyString();
        Pre responseContent = new Pre(responseJson);
        responseContent.addClassName("language-json");
        responseContent.getStyle().set("min-width", "0").set("overflow", "auto");

        dialog.add(responseContent);

        Button closeButton = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(closeButton);

        dialog.addOpenedChangeListener(event -> {
            if (event.isOpened()) {
                responseContent.getElement().executeJs("Prism.highlightElement(this);");
            }
        });

        dialog.open();
    }

    private Span createStatusBadge(int statusCode) {
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
}
