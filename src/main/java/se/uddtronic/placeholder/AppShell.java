package se.uddtronic.placeholder;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.AppShellSettings;
import com.vaadin.flow.theme.lumo.Lumo;

@Push
@StyleSheet("context://" + Lumo.STYLESHEET)
@StyleSheet("context://" + Lumo.UTILITY_STYLESHEET)
@StyleSheet("context://styles.css")
public class AppShell implements AppShellConfigurator {
    @Override
    public void configurePage(AppShellSettings settings) {
        settings.addFavIcon("icon", "/icon.svg", "192x192");
    }
}
