package se.uddtronic.placeholder.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.router.Layout;
import com.vaadin.flow.server.menu.MenuConfiguration;
import com.vaadin.flow.server.menu.MenuEntry;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.theme.lumo.LumoUtility;

import se.uddtronic.placeholder.ingestion.RequestCounterService;

@Layout
public final class MainLayout extends AppLayout {

    private final RequestCounterService requestCounterService;
    private Registration registration;
    private final H2 requestCounter = new H2();

    MainLayout(RequestCounterService requestCounterService) {
        this.requestCounterService = requestCounterService;

        setPrimarySection(Section.DRAWER);

        var drawerLayout = new VerticalLayout();
        drawerLayout.setSizeFull();

        drawerLayout.add(createHeader(), createSideNav(), createRequestCounterCard());
        addToDrawer(drawerLayout);
    }

    private Component createRequestCounterCard() {
        var card = new VerticalLayout();
        card.setSpacing(false);
        card.setPadding(false);
        card.setMargin(false);
        card.setAlignItems(Alignment.CENTER);
        card.addClassNames(LumoUtility.Background.CONTRAST_5, LumoUtility.Padding.Vertical.SMALL);

        var title = new Span("Total Requests");
        title.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);

        requestCounter.addClassNames(LumoUtility.Margin.NONE);

        card.add(title, requestCounter);
        return card;
    }

    @Override
    protected void onAttach(com.vaadin.flow.component.AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        UI ui = attachEvent.getUI();
        registration = requestCounterService
                .register(count -> ui.access(() -> requestCounter.setText(String.valueOf(count))));
    }

    @Override
    protected void onDetach(com.vaadin.flow.component.DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if (registration != null) {
            registration.remove();
        }
    }

    private Component createHeader() {
        var appLogo = new Image("/icon.svg", "Placeholder Mock");
        appLogo.setWidth("48px");
        appLogo.setHeight("48px");

        var appName = new Span("Placeholder Mock");
        appName.getStyle().set("font-weight", "bold");

        var header = new VerticalLayout(appLogo, appName);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.addClassNames(LumoUtility.Margin.Top.MEDIUM, LumoUtility.Margin.Bottom.MEDIUM);
        return header;
    }

    private SideNav createSideNav() {
        var nav = new SideNav();
        nav.setSizeFull();
        nav.getStyle().set("flex-grow", "1");
        MenuConfiguration.getMenuEntries().forEach(entry -> nav.addItem(createSideNavItem(entry)));
        return nav;
    }

    private SideNavItem createSideNavItem(MenuEntry menuEntry) {
        if (menuEntry.icon() != null) {
            return new SideNavItem(menuEntry.title(), menuEntry.path(), new Icon(menuEntry.icon()));
        } else {
            return new SideNavItem(menuEntry.title(), menuEntry.path());
        }
    }
}
