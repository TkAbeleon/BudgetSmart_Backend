# 🗄️ Base de Données — PostgreSQL

## Schéma complet

---

## Tables

### `users` — Comptes utilisateurs

```sql
CREATE TABLE users (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100)        NOT NULL,
    email       VARCHAR(150)        NOT NULL UNIQUE,
    password    VARCHAR(255)        NOT NULL,  -- bcrypt hash
    monthly_budget DECIMAL(12, 2)   DEFAULT 0,
    created_at  TIMESTAMP           DEFAULT NOW()
);
```

---

### `categories` — Catégories de transactions

```sql
CREATE TABLE categories (
    id      SERIAL PRIMARY KEY,
    user_id INTEGER         REFERENCES users(id) ON DELETE CASCADE,
    name    VARCHAR(100)    NOT NULL,
    type    VARCHAR(10)     NOT NULL CHECK (type IN ('EXPENSE', 'REVENUE')),
    color   VARCHAR(7)      DEFAULT '#6366f1'  -- couleur hex UI
);

-- Catégories par défaut insérées à l'inscription
-- Alimentation, Transport, Logement, Loisirs, Santé, Salaire, Autre
```

---

### `revenues` — Revenus

```sql
CREATE TABLE revenues (
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id INTEGER         REFERENCES categories(id) ON DELETE SET NULL,
    amount      DECIMAL(12, 2)  NOT NULL CHECK (amount > 0),
    description VARCHAR(255),
    date        DATE            NOT NULL DEFAULT CURRENT_DATE,
    created_at  TIMESTAMP       DEFAULT NOW()
);
```

---

### `expenses` — Dépenses

```sql
CREATE TABLE expenses (
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id INTEGER         REFERENCES categories(id) ON DELETE SET NULL,
    amount      DECIMAL(12, 2)  NOT NULL CHECK (amount > 0),
    description VARCHAR(255),
    date        DATE            NOT NULL DEFAULT CURRENT_DATE,
    created_at  TIMESTAMP       DEFAULT NOW()
);
```

---

### `savings_goals` — Objectifs d'épargne

```sql
CREATE TABLE savings_goals (
    id              SERIAL PRIMARY KEY,
    user_id         INTEGER         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            VARCHAR(150)    NOT NULL,
    target_amount   DECIMAL(12, 2)  NOT NULL CHECK (target_amount > 0),
    current_amount  DECIMAL(12, 2)  DEFAULT 0,
    deadline        DATE,
    created_at      TIMESTAMP       DEFAULT NOW()
);
```

---

### `budget_alerts` — Alertes budgétaires

```sql
CREATE TABLE budget_alerts (
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    level       VARCHAR(10)     NOT NULL CHECK (level IN ('WARNING', 'CRITICAL', 'MAX')),
    message     TEXT            NOT NULL,
    is_read     BOOLEAN         DEFAULT FALSE,
    created_at  TIMESTAMP       DEFAULT NOW()
);
```

---

## Triggers automatiques

### Fonction de vérification budgétaire

```sql
CREATE OR REPLACE FUNCTION check_budget_alert()
RETURNS TRIGGER AS $$
DECLARE
    v_budget        DECIMAL;
    v_total_spent   DECIMAL;
    v_percentage    DECIMAL;
    v_month         DATE;
BEGIN
    v_month := DATE_TRUNC('month', NEW.date);

    -- Récupérer le budget mensuel de l'utilisateur
    SELECT monthly_budget INTO v_budget
    FROM users WHERE id = NEW.user_id;

    -- Si pas de budget défini, on ne fait rien
    IF v_budget IS NULL OR v_budget = 0 THEN
        RETURN NEW;
    END IF;

    -- Calculer le total des dépenses du mois courant
    SELECT COALESCE(SUM(amount), 0) INTO v_total_spent
    FROM expenses
    WHERE user_id = NEW.user_id
      AND DATE_TRUNC('month', date) = v_month;

    v_percentage := (v_total_spent / v_budget) * 100;

    -- Alerte 80% — WARNING
    IF v_percentage >= 80 AND v_percentage < 90 THEN
        INSERT INTO budget_alerts (user_id, level, message)
        VALUES (
            NEW.user_id,
            'WARNING',
            FORMAT('⚠️ Vous avez consommé %.0f%% de votre budget mensuel.', v_percentage)
        );

    -- Alerte 90% — CRITICAL
    ELSIF v_percentage >= 90 AND v_percentage < 100 THEN
        INSERT INTO budget_alerts (user_id, level, message)
        VALUES (
            NEW.user_id,
            'CRITICAL',
            FORMAT('🔴 Attention ! %.0f%% de votre budget est utilisé.', v_percentage)
        );

    -- Alerte 100% — MAX
    ELSIF v_percentage >= 100 THEN
        INSERT INTO budget_alerts (user_id, level, message)
        VALUES (
            NEW.user_id,
            'MAX',
            FORMAT('🚨 Budget dépassé ! Vous avez dépensé %.0f%% de votre budget.', v_percentage)
        );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

### Attachement du trigger

```sql
CREATE TRIGGER trigger_budget_alert
AFTER INSERT ON expenses
FOR EACH ROW
EXECUTE FUNCTION check_budget_alert();
```

---

## Index de performance

```sql
-- Requêtes fréquentes par user + date
CREATE INDEX idx_expenses_user_date   ON expenses(user_id, date);
CREATE INDEX idx_revenues_user_date   ON revenues(user_id, date);
CREATE INDEX idx_alerts_user_unread   ON budget_alerts(user_id, is_read);
CREATE INDEX idx_savings_user         ON savings_goals(user_id);
```

---

## Vue utile : résumé mensuel

```sql
CREATE VIEW v_monthly_summary AS
SELECT
    u.id            AS user_id,
    DATE_TRUNC('month', CURRENT_DATE) AS month,
    COALESCE(SUM(e.amount), 0)  AS total_expenses,
    COALESCE(SUM(r.amount), 0)  AS total_revenues,
    u.monthly_budget            AS budget,
    CASE
        WHEN u.monthly_budget > 0
        THEN ROUND((COALESCE(SUM(e.amount), 0) / u.monthly_budget) * 100, 1)
        ELSE 0
    END AS budget_used_percent
FROM users u
LEFT JOIN expenses e ON e.user_id = u.id
    AND DATE_TRUNC('month', e.date) = DATE_TRUNC('month', CURRENT_DATE)
LEFT JOIN revenues r ON r.user_id = u.id
    AND DATE_TRUNC('month', r.date) = DATE_TRUNC('month', CURRENT_DATE)
GROUP BY u.id, u.monthly_budget;
```

---

## Données initiales (catégories par défaut)

```sql
-- Insérées via procédure appelée à l'inscription
CREATE OR REPLACE FUNCTION create_default_categories(p_user_id INTEGER)
RETURNS VOID AS $$
BEGIN
    INSERT INTO categories (user_id, name, type, color) VALUES
        (p_user_id, 'Alimentation',  'EXPENSE', '#ef4444'),
        (p_user_id, 'Transport',     'EXPENSE', '#f97316'),
        (p_user_id, 'Logement',      'EXPENSE', '#eab308'),
        (p_user_id, 'Loisirs',       'EXPENSE', '#8b5cf6'),
        (p_user_id, 'Santé',         'EXPENSE', '#06b6d4'),
        (p_user_id, 'Autre dépense', 'EXPENSE', '#6b7280'),
        (p_user_id, 'Salaire',       'REVENUE', '#22c55e'),
        (p_user_id, 'Freelance',     'REVENUE', '#10b981'),
        (p_user_id, 'Autre revenu',  'REVENUE', '#3b82f6');
END;
$$ LANGUAGE plpgsql;
```

---

## Diagramme entité-relation (simplifié)

```
users
  │
  ├──< categories (type: EXPENSE | REVENUE)
  │
  ├──< revenues ──> categories
  │
  ├──< expenses ──> categories
  │         │
  │         └── [TRIGGER] ──> budget_alerts
  │
  └──< savings_goals
```
