-- Données de démonstration pour BFB Automobile

-- Clients
INSERT INTO clients (id, nom, prenom, date_naissance, numero_permis, adresse, date_creation, actif) VALUES
(1, 'Dupont', 'Jean', '1985-03-15', '123456789', '10 rue de la Paix, 75001 Paris', CURRENT_DATE, true),
(2, 'Martin', 'Sophie', '1990-07-22', '987654321', '25 avenue des Champs, 69001 Lyon', CURRENT_DATE, true),
(3, 'Bernard', 'Pierre', '1988-11-08', '456789123', '5 boulevard Victor Hugo, 33000 Bordeaux', CURRENT_DATE, true),
(4, 'Dubois', 'Marie', '1995-02-14', '789123456', '15 rue Pasteur, 59000 Lille', CURRENT_DATE, true),
(5, 'Robert', 'Thomas', '1982-09-30', '321654987', '30 place de la Mairie, 44000 Nantes', CURRENT_DATE, true);

-- Véhicules
INSERT INTO vehicules (id, marque, modele, motorisation, couleur, immatriculation, date_acquisition, etat) VALUES
(1, 'Peugeot', '308', '1.5 BlueHDi 130ch', 'Gris', 'AB-123-CD', '2023-01-15', 'DISPONIBLE'),
(2, 'Renault', 'Clio', '1.0 TCe 90ch', 'Blanc', 'EF-456-GH', '2023-03-20', 'DISPONIBLE'),
(3, 'Citroën', 'C3', '1.2 PureTech 83ch', 'Rouge', 'IJ-789-KL', '2023-02-10', 'DISPONIBLE'),
(4, 'Volkswagen', 'Golf', '1.4 TSI 125ch', 'Noir', 'MN-012-OP', '2022-11-05', 'DISPONIBLE'),
(5, 'Toyota', 'Yaris', 'Hybrid 116ch', 'Bleu', 'QR-345-ST', '2023-05-12', 'DISPONIBLE'),
(6, 'Ford', 'Fiesta', '1.0 EcoBoost 95ch', 'Vert', 'UV-678-WX', '2023-04-18', 'EN_LOCATION'),
(7, 'Opel', 'Corsa', '1.2 Turbo 100ch', 'Argent', 'YZ-901-AB', '2023-06-25', 'EN_PANNE');

-- Contrats
-- Contrat terminé (historique)
INSERT INTO contrats (id, date_debut, date_fin, etat, client_id, vehicule_id, date_creation, commentaire) VALUES
(1, '2024-11-01', '2024-11-10', 'TERMINE', 1, 2, '2024-10-25', 'Location pour voyage professionnel');

-- Contrat en cours
INSERT INTO contrats (id, date_debut, date_fin, etat, client_id, vehicule_id, date_creation, commentaire) VALUES
(2, '2024-11-15', '2024-11-25', 'EN_COURS', 2, 6, '2024-11-10', 'Location vacances');

-- Contrat en attente
INSERT INTO contrats (id, date_debut, date_fin, etat, client_id, vehicule_id, date_creation, commentaire) VALUES
(3, '2024-12-01', '2024-12-15', 'EN_ATTENTE', 3, 1, '2024-11-17', 'Réservation pour les fêtes');

-- Contrat en attente (différent véhicule)
INSERT INTO contrats (id, date_debut, date_fin, etat, client_id, vehicule_id, date_creation, commentaire) VALUES
(4, '2024-12-10', '2024-12-20', 'EN_ATTENTE', 4, 3, '2024-11-17', 'Location pour déménagement');

-- Séquences pour les IDs auto-générés
ALTER TABLE clients ALTER COLUMN id RESTART WITH 6;
ALTER TABLE vehicules ALTER COLUMN id RESTART WITH 8;
ALTER TABLE contrats ALTER COLUMN id RESTART WITH 5;
