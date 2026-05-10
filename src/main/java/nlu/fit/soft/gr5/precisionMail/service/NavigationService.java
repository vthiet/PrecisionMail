package nlu.fit.soft.gr5.precisionMail.service;

import java.util.function.Consumer;

public class NavigationService {
    private static NavigationService instance;

    // Listener channel chuyển màn hìnhh
    private Consumer<String> onNavigate;

    public static NavigationService getInstance(){
        if (instance == null) instance = new NavigationService();
        return instance;
    }

    // Dang ky nhan thong bao
    public  void setNavigationListener(Consumer<String> listener){
        this.onNavigate = listener;
    }


    public void navigateTo(String fxmlFileName){
        System.out.println("[Service] Nhận yêu cầu chuyển sang: " + fxmlFileName);

        if (fxmlFileName != null) onNavigate.accept(fxmlFileName);
    }
}
