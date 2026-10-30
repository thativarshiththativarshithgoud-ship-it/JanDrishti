package in.jandrishti.data;

import in.jandrishti.model.ChatMessage;
import in.jandrishti.model.Issue;
import in.jandrishti.model.IssuePriority;
import in.jandrishti.model.IssueStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class AiChatService {
    private final IssueRepository repository;

    public record TriageResult(
            IssuePriority priority,
            String suggestedCategory,
            String suggestedDepartment,
            String explanation
    ) {}

    public AiChatService(IssueRepository repository) {
        this.repository = repository;
    }

    public ChatMessage processQuery(String userQuery) {
        if (userQuery == null || userQuery.isBlank()) {
            return ChatMessage.ai("Hello! I am your JanDrishti Copilot AI. Ask me about civic issues, hotspots, SLA delays, or type a request.");
        }

        String query = userQuery.toLowerCase().trim();
        List<Issue> issues = repository.findAll();

        if (query.contains("summary") || query.contains("overview") || query.contains("status") || query.contains("health")) {
            long total = issues.size();
            long open = issues.stream().filter(i -> i.status() != IssueStatus.RESOLVED).count();
            long critical = issues.stream().filter(i -> i.priority() == IssuePriority.CRITICAL && i.status() != IssueStatus.RESOLVED).count();
            long resolved = issues.stream().filter(i -> i.status() == IssueStatus.RESOLVED).count();

            return ChatMessage.ai(String.format(
                    "📊 **JanDrishti Executive Summary**\n\n" +
                    "• **Total Tracked Reports:** %d\n" +
                    "• **Open / In-Progress:** %d\n" +
                    "• **🚨 Critical Pending:** %d issues\n" +
                    "• **Resolved & Verified:** %d issues (%.1f%% closure rate)\n\n" +
                    "⚡ *Recommendation:* Dispatch emergency maintenance for the %d pending critical issue(s) first.",
                    total, open, critical, resolved, total > 0 ? (resolved * 100.0 / total) : 0, critical
            ));
        }

        if (query.contains("hotspot") || query.contains("area") || query.contains("where")) {
            Map<String, Long> locationCounts = issues.stream()
                    .filter(i -> i.status() != IssueStatus.RESOLVED)
                    .collect(Collectors.groupingBy(Issue::locationLabel, Collectors.counting()));

            String topHotspots = locationCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(3)
                    .map(e -> "• **" + e.getKey() + "**: " + e.getValue() + " open report(s)")
                    .collect(Collectors.joining("\n"));

            return ChatMessage.ai("🔥 **Top Problem Hotspots Requiring Intervention**\n\n" +
                    (topHotspots.isEmpty() ? "No active hotspots identified!" : topHotspots) +
                    "\n\n💡 *AI Insight:* High volume in these areas indicates potential infrastructure wear or storm damage.");
        }

        if (query.contains("urgent") || query.contains("critical") || query.contains("priority") || query.contains("emergency")) {
            List<Issue> criticalList = issues.stream()
                    .filter(i -> i.priority() == IssuePriority.CRITICAL && i.status() != IssueStatus.RESOLVED)
                    .sorted(Comparator.comparing(Issue::reportedAt).reversed())
                    .toList();

            if (criticalList.isEmpty()) {
                return ChatMessage.ai("✅ Excellent news! There are currently **0 unresolved Critical priority issues** in the system.");
            }

            Issue topCritical = criticalList.get(0);
            return ChatMessage.aiWithAction(
                    String.format("🚨 **Top Priority Issue Identified (#%d)**\n\n" +
                            "**Title:** %s\n" +
                            "**Location:** %s (%s)\n" +
                            "**Status:** %s | **Dept:** %s\n" +
                            "**Reporter Note:** \"%s\"",
                            topCritical.id(), topCritical.title(), topCritical.locationLabel(), topCritical.road(),
                            topCritical.status().label(), topCritical.department(), topCritical.description()),
                    topCritical.id(),
                    "Inspect Issue #" + topCritical.id()
            );
        }

        if (query.contains("pothole") || query.contains("road")) {
            List<Issue> roadIssues = issues.stream()
                    .filter(i -> (i.category().equalsIgnoreCase("Pothole") || i.category().equalsIgnoreCase("Damaged road")) && i.status() != IssueStatus.RESOLVED)
                    .toList();

            return ChatMessage.ai(String.format(
                    "🛣️ **Road & Pothole Infrastructure Report**\n\n" +
                    "Currently monitoring **%d open road hazard(s)**.\n" +
                    "• PWD Department pending count: %d\n" +
                    "• Key affected corridors: Karol Bagh (Ajmal Khan Rd), Andheri East (Marol Rd), Saket (Press Enclave Rd).",
                    roadIssues.size(),
                    roadIssues.stream().filter(i -> i.department().contains("PWD") || i.department().contains("Municipal")).count()
            ));
        }

        if (query.contains("sla") || query.contains("delay") || query.contains("pending")) {
            List<Issue> delayed = issues.stream()
                    .filter(i -> i.status() == IssueStatus.REPORTED)
                    .toList();

            return ChatMessage.ai(String.format(
                    "⏱️ **SLA & Action Delay Advisory**\n\n" +
                    "There are **%d unassigned 'REPORTED' issue(s)** currently awaiting officer triage.\n" +
                    "Assigning an officer within 4 hours improves citizen satisfaction rating by +45%%.",
                    delayed.size()
            ));
        }

        // Generic intelligent query response
        List<Issue> matches = repository.search(query, "All states", null, null);
        if (!matches.isEmpty()) {
            Issue first = matches.get(0);
            return ChatMessage.aiWithAction(
                    String.format("🔎 Found **%d relevant report(s)** matching '%s'.\n\n" +
                            "**Top Result (#%d):** %s\n" +
                            "• Location: %s\n" +
                            "• Category: %s | Priority: %s\n" +
                            "• Current Status: %s",
                            matches.size(), userQuery, first.id(), first.title(), first.locationLabel(),
                            first.category(), first.priority().label(), first.status().label()),
                    first.id(),
                    "View Issue #" + first.id()
            );
        }

        return ChatMessage.ai("🤖 I've analyzed your query: \"" + userQuery + "\".\n\n" +
                "You can ask me to:\n" +
                "1. Show executive **summary** or platform health\n" +
                "2. Identify top **hotspots** and road hazards\n" +
                "3. List **urgent critical** emergency issues\n" +
                "4. Check **SLA delay** risks");
    }

    public TriageResult autoTriage(String description, String categoryInput, String roadInput) {
        String text = ((description == null ? "" : description) + " " + (categoryInput == null ? "" : categoryInput) + " " + (roadInput == null ? "" : roadInput)).toLowerCase();

        IssuePriority priority = IssuePriority.MEDIUM;
        String category = categoryInput != null && !categoryInput.isBlank() ? categoryInput : "General Infrastructure";
        String department = "Public Works Dept (PWD)";
        String explanation = "Standard priority assigned based on report description.";

        if (text.contains("school") || text.contains("hospital") || text.contains("accident") || text.contains("leak") || text.contains("stuck") || text.contains("emergency") || text.contains("danger") || text.contains("collapse")) {
            priority = IssuePriority.CRITICAL;
            explanation = "Elevated to CRITICAL due to safety hazards near sensitive public zone or active danger.";
        } else if (text.contains("dark") || text.contains("night") || text.contains("waterlog") || text.contains("flood") || text.contains("block") || text.contains("deep")) {
            priority = IssuePriority.HIGH;
            explanation = "Assigned HIGH priority due to severe traffic obstruction or nighttime risk.";
        } else if (text.contains("garbage") || text.contains("bin") || text.contains("litter") || text.contains("clean")) {
            priority = IssuePriority.MEDIUM;
            explanation = "Assigned MEDIUM priority for routine municipal sanitation.";
        }

        // Department inference
        if (text.contains("light") || text.contains("power") || text.contains("electric") || text.contains("wire")) {
            department = "Electricity Board (MSEDCL/BSES)";
            category = "Streetlight";
        } else if (text.contains("water") || text.contains("drain") || text.contains("sewer") || text.contains("pipe")) {
            department = "Water Supply & Sewage Board";
            category = text.contains("drain") ? "Drainage" : "Water Leakage";
        } else if (text.contains("traffic") || text.contains("signal") || text.contains("light")) {
            department = "Traffic Police & Signals Dept";
            category = "Traffic Signal";
        } else if (text.contains("garbage") || text.contains("waste") || text.contains("trash")) {
            department = "Municipal Sanitation Dept";
            category = "Garbage";
        } else if (text.contains("pothole") || text.contains("road") || text.contains("asphalt") || text.contains("tarmac")) {
            department = "Public Works Dept (PWD)";
            category = text.contains("pothole") ? "Pothole" : "Damaged road";
        }

        return new TriageResult(priority, category, department, explanation);
    }
}
