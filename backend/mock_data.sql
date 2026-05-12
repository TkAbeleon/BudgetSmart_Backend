-- ==========================================
-- 1. TRIGGER : Alertes de dépassement de budget
-- ==========================================
CREATE OR REPLACE FUNCTION check_budget_alert()
RETURNS TRIGGER AS $$
DECLARE
    v_total_expenses NUMERIC;
    v_budget NUMERIC;
    v_percent NUMERIC;
    v_alert_level VARCHAR;
    v_message VARCHAR;
    v_month_start DATE;
    v_month_end DATE;
BEGIN
    -- Obtenir le budget mensuel de l'utilisateur
    SELECT monthly_budget INTO v_budget FROM users WHERE id = NEW.user_id;

    -- Si pas de budget ou budget = 0, on ne fait rien
    IF v_budget IS NULL OR v_budget = 0 THEN
        RETURN NEW;
    END IF;

    -- Calculer le début et la fin du mois pour la dépense insérée
    v_month_start := date_trunc('month', NEW.date)::DATE;
    v_month_end := (date_trunc('month', NEW.date) + interval '1 month' - interval '1 day')::DATE;

    -- Somme des dépenses pour ce mois
    SELECT COALESCE(SUM(amount), 0) INTO v_total_expenses
    FROM expenses
    WHERE user_id = NEW.user_id
    AND date >= v_month_start AND date <= v_month_end;

    -- Calcul du pourcentage
    v_percent := (v_total_expenses / v_budget) * 100;

    -- Déterminer le niveau d'alerte
    IF v_percent >= 100 THEN
        v_alert_level := 'CRITICAL';
        v_message := '🔴 Attention ! Vous avez dépassé votre budget mensuel (' || round(v_percent, 1) || '% consommé).';
    ELSIF v_percent >= 80 THEN
        v_alert_level := 'WARNING';
        v_message := '⚠️ Vous approchez de la limite : ' || round(v_percent, 1) || '% de votre budget mensuel.';
    ELSE
        RETURN NEW;
    END IF;

    -- Insérer l'alerte
    INSERT INTO budget_alerts (user_id, level, message, is_read, created_at)
    VALUES (NEW.user_id, v_alert_level, v_message, false, CURRENT_TIMESTAMP);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trigger_check_budget ON expenses;
CREATE TRIGGER trigger_check_budget
AFTER INSERT OR UPDATE OF amount ON expenses
FOR EACH ROW
EXECUTE FUNCTION check_budget_alert();


-- ==========================================
-- 2. DONNÉES DE TEST (RICHE, depuis Janvier 2025)
-- ==========================================

-- A. Nettoyer les données de demo existantes si elles sont là
DELETE FROM users WHERE email = 'demo@budgetsmart.mg';

-- B. Création du User (le mot de passe est "password123")
INSERT INTO users (first_name, last_name, name, email, password, monthly_budget, created_at)
VALUES ('Demo', 'Budget', 'Demo Budget', 'demo@budgetsmart.mg', '$2a$10$X8j3nJqJ1vH7y1gZ/jJ2OO.5bE7fO.U3jVz/5x6i/2fX9o/oE1w/u', 2500.00, '2025-01-01 08:00:00');

DO $$
DECLARE
    v_user_id INT;
    
    -- Variables catégories Expenses
    v_c_log INT;
    v_c_ali INT;
    v_c_tra INT;
    v_c_loi INT;
    v_c_abo INT;
    v_c_san INT;

    -- Variables catégories Revenues
    v_c_sal INT;
    v_c_fre INT;
    v_c_inv INT;

BEGIN
    SELECT id INTO v_user_id FROM users WHERE email = 'demo@budgetsmart.mg';

    -- ==========================================
    -- CATÉGORIES
    -- ==========================================
    INSERT INTO categories (user_id, name, type, color) VALUES (v_user_id, 'Logement', 'EXPENSE', '#eab308') RETURNING id INTO v_c_log;
    INSERT INTO categories (user_id, name, type, color) VALUES (v_user_id, 'Alimentation', 'EXPENSE', '#ef4444') RETURNING id INTO v_c_ali;
    INSERT INTO categories (user_id, name, type, color) VALUES (v_user_id, 'Transport', 'EXPENSE', '#f97316') RETURNING id INTO v_c_tra;
    INSERT INTO categories (user_id, name, type, color) VALUES (v_user_id, 'Loisirs & Sorties', 'EXPENSE', '#8b5cf6') RETURNING id INTO v_c_loi;
    INSERT INTO categories (user_id, name, type, color) VALUES (v_user_id, 'Abonnements', 'EXPENSE', '#3b82f6') RETURNING id INTO v_c_abo;
    INSERT INTO categories (user_id, name, type, color) VALUES (v_user_id, 'Santé', 'EXPENSE', '#ec4899') RETURNING id INTO v_c_san;
    
    INSERT INTO categories (user_id, name, type, color) VALUES (v_user_id, 'Salaire Principal', 'REVENUE', '#22c55e') RETURNING id INTO v_c_sal;
    INSERT INTO categories (user_id, name, type, color) VALUES (v_user_id, 'Freelance', 'REVENUE', '#10b981') RETURNING id INTO v_c_fre;
    INSERT INTO categories (user_id, name, type, color) VALUES (v_user_id, 'Investissements', 'REVENUE', '#06b6d4') RETURNING id INTO v_c_inv;

    -- ==========================================
    -- REVENUS (Janvier -> Mai)
    -- ==========================================
    INSERT INTO revenues (user_id, category_id, amount, description, date, created_at) VALUES
    -- Janvier
    (v_user_id, v_c_sal, 3000.00, 'Salaire Janvier', '2025-01-01', '2025-01-01 10:00:00'),
    (v_user_id, v_c_fre, 450.00, 'Projet Logo', '2025-01-15', '2025-01-15 10:00:00'),
    -- Février
    (v_user_id, v_c_sal, 3000.00, 'Salaire Février', '2025-02-01', '2025-02-01 10:00:00'),
    (v_user_id, v_c_inv, 120.50, 'Dividendes', '2025-02-28', '2025-02-28 10:00:00'),
    -- Mars
    (v_user_id, v_c_sal, 3000.00, 'Salaire Mars', '2025-03-01', '2025-03-01 10:00:00'),
    (v_user_id, v_c_fre, 800.00, 'Site Vitrine', '2025-03-20', '2025-03-20 10:00:00'),
    -- Avril
    (v_user_id, v_c_sal, 3000.00, 'Salaire Avril', '2025-04-01', '2025-04-01 10:00:00'),
    -- Mai
    (v_user_id, v_c_sal, 3000.00, 'Salaire Mai', '2025-05-01', '2025-05-01 10:00:00');

    -- ==========================================
    -- DÉPENSES (Mois normaux: Janvier à Avril)
    -- ==========================================
    -- Janvier (Total ~ 1800)
    INSERT INTO expenses (user_id, category_id, amount, description, date, created_at) VALUES
    (v_user_id, v_c_log, 900.00, 'Loyer Janvier', '2025-01-02', '2025-01-02 08:00:00'),
    (v_user_id, v_c_abo, 45.00, 'Internet + Mobile', '2025-01-05', '2025-01-05 08:00:00'),
    (v_user_id, v_c_abo, 15.00, 'Netflix', '2025-01-08', '2025-01-08 08:00:00'),
    (v_user_id, v_c_ali, 150.00, 'Courses Super U', '2025-01-05', '2025-01-05 18:00:00'),
    (v_user_id, v_c_ali, 120.00, 'Marché', '2025-01-12', '2025-01-12 10:00:00'),
    (v_user_id, v_c_ali, 180.00, 'Courses Leclerc', '2025-01-20', '2025-01-20 18:00:00'),
    (v_user_id, v_c_tra, 60.00, 'Plein Essence', '2025-01-10', '2025-01-10 14:00:00'),
    (v_user_id, v_c_tra, 60.00, 'Plein Essence', '2025-01-25', '2025-01-25 14:00:00'),
    (v_user_id, v_c_loi, 80.00, 'Restaurant Amis', '2025-01-14', '2025-01-14 21:00:00'),
    (v_user_id, v_c_loi, 40.00, 'Ciné', '2025-01-22', '2025-01-22 20:00:00');

    -- Février (Total ~ 1700)
    INSERT INTO expenses (user_id, category_id, amount, description, date, created_at) VALUES
    (v_user_id, v_c_log, 900.00, 'Loyer Février', '2025-02-02', '2025-02-02 08:00:00'),
    (v_user_id, v_c_abo, 60.00, 'Abonnements (Net/Tel/TV)', '2025-02-05', '2025-02-05 08:00:00'),
    (v_user_id, v_c_ali, 380.00, 'Courses globales', '2025-02-15', '2025-02-15 18:00:00'),
    (v_user_id, v_c_tra, 60.00, 'Plein Essence', '2025-02-10', '2025-02-10 14:00:00'),
    (v_user_id, v_c_loi, 120.00, 'Soirée anniversaire', '2025-02-20', '2025-02-20 22:00:00'),
    (v_user_id, v_c_san, 50.00, 'Pharmacie', '2025-02-18', '2025-02-18 10:00:00');

    -- Mars (Total ~ 1900)
    INSERT INTO expenses (user_id, category_id, amount, description, date, created_at) VALUES
    (v_user_id, v_c_log, 900.00, 'Loyer Mars', '2025-03-02', '2025-03-02 08:00:00'),
    (v_user_id, v_c_abo, 60.00, 'Abonnements', '2025-03-05', '2025-03-05 08:00:00'),
    (v_user_id, v_c_ali, 450.00, 'Courses', '2025-03-12', '2025-03-12 18:00:00'),
    (v_user_id, v_c_tra, 120.00, 'Essence', '2025-03-15', '2025-03-15 14:00:00'),
    (v_user_id, v_c_loi, 200.00, 'Concert', '2025-03-25', '2025-03-25 21:00:00'),
    (v_user_id, v_c_loi, 150.00, 'Vêtements', '2025-03-28', '2025-03-28 15:00:00');

    -- Avril (Total ~ 1600)
    INSERT INTO expenses (user_id, category_id, amount, description, date, created_at) VALUES
    (v_user_id, v_c_log, 900.00, 'Loyer Avril', '2025-04-02', '2025-04-02 08:00:00'),
    (v_user_id, v_c_abo, 60.00, 'Abonnements', '2025-04-05', '2025-04-05 08:00:00'),
    (v_user_id, v_c_ali, 400.00, 'Courses', '2025-04-12', '2025-04-12 18:00:00'),
    (v_user_id, v_c_tra, 60.00, 'Essence', '2025-04-15', '2025-04-15 14:00:00'),
    (v_user_id, v_c_san, 150.00, 'Dentiste', '2025-04-20', '2025-04-20 10:00:00');


    -- ==========================================
    -- MAI 2025 : SCÉNARIO DE DÉPASSEMENT
    -- Budget de l'utilisateur = 2500.00
    -- ==========================================
    
    -- 1. Loyer : 900 (Cumul: 900 => 36% du budget)
    INSERT INTO expenses (user_id, category_id, amount, description, date, created_at) VALUES
    (v_user_id, v_c_log, 900.00, 'Loyer Mai', '2025-05-02', CURRENT_TIMESTAMP - INTERVAL '10 days');

    -- 2. Abonnements et courses : 500 (Cumul: 1400 => 56% du budget)
    INSERT INTO expenses (user_id, category_id, amount, description, date, created_at) VALUES
    (v_user_id, v_c_abo, 60.00, 'Abonnements', '2025-05-05', CURRENT_TIMESTAMP - INTERVAL '7 days'),
    (v_user_id, v_c_ali, 440.00, 'Courses', '2025-05-08', CURRENT_TIMESTAMP - INTERVAL '4 days');

    -- 3. Achat imprévu : 700 (Cumul: 2100 => 84% du budget) 
    -- ⚠️ DÉCLENCHE L'ALERTE WARNING (>80%)
    INSERT INTO expenses (user_id, category_id, amount, description, date, created_at) VALUES
    (v_user_id, v_c_loi, 700.00, 'Nouvelle TV 4K', '2025-05-10', CURRENT_TIMESTAMP - INTERVAL '2 days');

    -- 4. Soirée chère : 450 (Cumul: 2550 => 102% du budget)
    -- 🔴 DÉCLENCHE L'ALERTE CRITICAL (>100%)
    INSERT INTO expenses (user_id, category_id, amount, description, date, created_at) VALUES
    (v_user_id, v_c_loi, 450.00, 'Festival et Restaurant', '2025-05-11', CURRENT_TIMESTAMP);


    -- ==========================================
    -- ÉPARGNE
    -- ==========================================
    INSERT INTO savings_goals (user_id, name, target_amount, current_amount, target_date, created_at) VALUES
    (v_user_id, 'Voyage Bali', 4000.00, 1500.00, '2025-11-01', '2025-01-10 10:00:00'),
    (v_user_id, 'Apport Maison', 20000.00, 4500.00, '2027-01-01', '2025-01-15 10:00:00'),
    (v_user_id, 'Fonds de sécurité', 5000.00, 5000.00, NULL, '2025-01-15 10:00:00');

END $$;
