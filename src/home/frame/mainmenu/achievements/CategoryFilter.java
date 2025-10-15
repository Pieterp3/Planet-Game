package home.frame.mainmenu.achievements;

public enum CategoryFilter {
    ALL("All Challenges"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    TIME_BASED("Speed Runs"),
    ABILITIES("Abilities"),
    CONQUEST("Conquest"),
    PROGRESSION("Progression");

    private final String displayName;

    CategoryFilter(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
