package in.jandrishti.model;

public enum IssuePriority {
    CRITICAL("Critical", "priority-critical"),
    HIGH("High", "priority-high"),
    MEDIUM("Medium", "priority-medium"),
    LOW("Low", "priority-low");

    private final String label;
    private final String styleClass;

    IssuePriority(String label, String styleClass) {
        this.label = label;
        this.styleClass = styleClass;
    }

    public String label() {
        return label;
    }

    public String styleClass() {
        return styleClass;
    }

    public static IssuePriority fromString(String text) {
        if (text == null) return MEDIUM;
        for (IssuePriority p : values()) {
            if (p.label.equalsIgnoreCase(text) || p.name().equalsIgnoreCase(text)) {
                return p;
            }
        }
        return MEDIUM;
    }
}
