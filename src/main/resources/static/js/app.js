// Progressive enhancement - the site works fine with JS disabled, this
// just adds the dark-mode toggle and the Profile dropdown interaction.

const THEME_STORAGE_KEY = "hirezen-theme";

function applyTheme(theme) {
    document.documentElement.setAttribute("data-theme", theme);
    document.querySelectorAll("[data-theme-icon]").forEach((el) => {
        // Sun-ish glyph in dark mode ("switch back to light"),
        // moon-ish glyph in light mode ("switch to dark").
        el.textContent = theme === "dark" ? "\u2600" : "\u263D";
    });
}

document.addEventListener("DOMContentLoaded", () => {

    // ---- Dark mode toggle ----
    const savedTheme = localStorage.getItem(THEME_STORAGE_KEY) || "light";
    applyTheme(savedTheme);

    document.querySelectorAll("[data-theme-toggle]").forEach((btn) => {
        btn.addEventListener("click", () => {
            const current = document.documentElement.getAttribute("data-theme") || "light";
            const next = current === "dark" ? "light" : "dark";
            applyTheme(next);
            localStorage.setItem(THEME_STORAGE_KEY, next);
        });
    });

    // ---- Profile dropdown ----
    document.querySelectorAll("[data-profile-menu]").forEach((menu) => {
        const trigger = menu.querySelector("[data-profile-menu-trigger]");
        if (!trigger) return;

        trigger.addEventListener("click", (e) => {
            e.stopPropagation();
            menu.classList.toggle("open");
        });
    });

    // Close any open dropdown when clicking elsewhere, or on Escape.
    document.addEventListener("click", () => {
        document.querySelectorAll(".profile-menu.open").forEach((menu) => {
            menu.classList.remove("open");
        });
    });

    document.addEventListener("keydown", (e) => {
        if (e.key === "Escape") {
            document.querySelectorAll(".profile-menu.open").forEach((menu) => {
                menu.classList.remove("open");
            });
        }
    });
});
