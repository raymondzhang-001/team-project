package app;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AppBuilder builder = new AppBuilder();

            JFrame app = builder
                    .addSearchView()          // 🔥 반드시 제일 먼저
                    .addSearchUseCase()
                    .addSaveStopsUseCase()
                    .addSuggestionUseCase()
                    .addRemoveMarkerUseCase()
                    .loadStopsOnStartup()     // 🔥 여기서 호출
                    .build();                 // 마지막에 build

            app.pack();
            app.setLocationRelativeTo(null);
            app.setVisible(true);
        });
    }
}
