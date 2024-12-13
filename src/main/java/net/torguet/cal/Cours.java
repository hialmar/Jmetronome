package net.torguet.cal;

import java.time.ZonedDateTime;

public class Cours {
    private String intitule;
    private String enseignant;
    private TypeCours type;
    private ZonedDateTime debut;
    private float duree;
    private boolean enParallele;
    private String salle;
    private String groupe;

    public Cours(String intitule) {
        this.intitule = intitule;
        this.type = TypeCours.TYPE_AUTRE;
        this.debut = null;
        this.duree = 0;
        this.enParallele = false;
    }

    public String getIntitule() {
        return intitule;
    }

    public void setIntitule(String intitule) {
        this.intitule = intitule;
    }

    public TypeCours getType() {
        return type;
    }

    public void setType(TypeCours type) {
        this.type = type;
    }

    public ZonedDateTime getDebut() {
        return debut;
    }

    public void setDebut(ZonedDateTime debut) {
        this.debut = debut;
    }

    public float getDuree() {
        return duree;
    }

    public void setDuree(float duree) {
        this.duree = duree;
    }

    public boolean isEnParallele() {
        return enParallele;
    }

    public void setEnParallele(boolean enParallele) {
        this.enParallele = enParallele;
    }

    public String getEnseignant() {
        return enseignant;
    }

    public void setEnseignant(String enseignant) {
        this.enseignant = enseignant;
    }

    public String getSalle() {
        return salle;
    }

    public void setSalle(String salle) {
        this.salle = salle;
    }

    public String getGroupe() {
        return groupe;
    }

    public void setGroupe(String groupe) {
        this.groupe = groupe;
    }

    /**
     * Vérifie si le cours courant matche avec le cours exemple
     * @param cours le cours exemple, si null ou vide matche toujours
     * @param matchAny si vrai, matche dès qu'au moins un attribut matche sinon matche tout
     * @return true si ça matche
     */
    public boolean match(Cours cours, boolean matchAny) {
        if (cours == null) {
            return true;
        }
        boolean result = true;
        if (this.intitule != null && cours.getIntitule() != null) {
            if (this.intitule.contains(cours.getIntitule())) {
                if (matchAny) {
                    return true;
                }
            } else {
                result = false;
            }
        }
        if (this.enseignant != null && cours.getEnseignant() != null) {
            if (this.enseignant.contains(cours.getEnseignant())) {
                if (matchAny) {
                    return true;
                }
            } else {
                result = false;
            }
        }
        if (cours.getType() != null && cours.getType() != TypeCours.TYPE_AUTRE) {
            if (this.type.equals(cours.getType())) {
                if (matchAny) {
                    return true;
                }
            } else {
                result = false;
            }
        }
        if (this.debut != null && cours.getDebut() != null) {
            if (this.debut.isBefore(cours.getDebut())) {
                if (matchAny) {
                    return true;
                }
            } else {
                result = false;
            }
        }
        if (this.duree > 0 && cours.getDuree() > 0) {
            if (this.duree == cours.getDuree()) {
                if (matchAny) {
                    return true;
                }
            } else {
                result = false;
            }
        }
        if (this.groupe != null && cours.getGroupe() != null) {
            if (this.groupe.equals(cours.getGroupe())) {
                if (matchAny) {
                    return true;
                }
            } else {
                result = false;
            }
        }
        if (this.salle != null && cours.getSalle() != null) {
            if (this.salle.contains(cours.getSalle())) {
                if (matchAny) {
                    return true;
                }
            } else {
                result = false;
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return  (type == TypeCours.TYPE_CONTROLE ? "Contrôle " : "") +
                (type == TypeCours.TYPE_INGE ? "Ingé " : "") +
                (type == TypeCours.TYPE_IRT ? "L3 IRT " : "") +
                (type == TypeCours.TYPE_ALTERNANCE ? "ALT " : "") +
                intitule +
                (enseignant != null ? ", ens : " + enseignant : "") +
                (groupe != null ? ", grp : " + groupe : "") +
                (salle != null ? ", salle : " + salle : "");
    }
}
