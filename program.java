import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.variables.integer.IntVar;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static choco.Choco.*;

public class program {
    public static void main(String[] args) {
        // Nombre des options
        int nbOptions = 5;

        // Nombre des categories
        int nbCategories = 6;

        // Nombre total des voiture a produire
        int nbPositions = 10;

        // nombre maximum de voitures P / une sous-séquence de taille Q
        int[] P = {1, 2, 1, 2, 1};
        int[] Q= {2, 3, 3, 5, 5};

        // Nombre de voiture demandé par categorie.
        int[] D = {1, 1, 2, 2, 2, 2};

        //Les options de chaque categorie
        int[][] options =
                {
                        {1, 0, 1, 1, 0},
                        {0, 0, 0, 1, 0},
                        {0, 1, 0, 0, 1},
                        {0, 1, 0, 1, 0},
                        {1, 0, 1, 0, 0},
                        {1, 1, 0, 0, 0}
                };


        CPModel m = new CPModel();

        // La sequence des voitures à produire S[i] contient le numéro de catétogie de cette voiture
        IntegerVariable[] S;
        S = makeIntVarArray("S", nbPositions, 0, nbCategories - 1);

        // OPT[i][j] vaut 1 si la categorie i est dans S[j]
        IntegerVariable[][] OPT;
        OPT = makeIntVarArray("OPT", nbOptions, nbPositions, 0, 1);

        //Les contraintes
        //C1: Contrainte qui precise le nombre de voiture demandé par chaque catégorie.
        int[] classes = new int[nbCategories];
        IntegerVariable[] classDemand = new IntegerVariable[nbCategories];
        for (int j = 0; j < nbCategories; j++) {
            classes[j] = j;
            classDemand[j] = makeIntVar("classDemand[" + j + "]", D[j], D[j]);
        }
        m.addConstraint(globalCardinality(S, classes, classDemand));

        //C2: Contrainte qui précise les options de chaque voiture selon sa catégorie.
        for (int cat = 0; cat < nbCategories; cat++) {
            for (int car = 0; car < nbPositions; car++) {
                Constraint[] C = new Constraint[nbOptions];
                for (int op = 0; op < nbOptions; op++)
                    C[op] = eq(OPT[op][car], options[cat][op]);

                m.addConstraint(ifOnlyIf(and(C), eq(S[car], cat)));
            }
        }

        //C3: Contrainte qui précise le nombre maximum de voitures P possédant une option opt qui peuvent être produites sur une sous-séquence de taille Q.
        for (int opt = 0; opt < nbOptions; opt++) {
            for (int i = 0; i < nbPositions; i += Q[opt]) {
                IntegerVariable[] v = new IntegerVariable[Q[opt]];
                boolean test = true;
                for (int j = 0; j < Q[opt]; j++) {
                    if (i + j >= nbPositions) {
                        test = false;
                        break;
                    }
                    v[j] = OPT[opt][i + j];
                }
                if (test)
                    m.addConstraint(Choco.leq(sum(v), P[opt]));
            }
        }

        //Solveur
        CPSolver s = new CPSolver();
        s.read(m);
        s.solve();

        //L'affichage de la solution
        System.out.println("Classe  Options requises");
        for (int p = 0; p < nbPositions; p++) {
            System.out.print("  " + s.getVar(S[p]).getVal() + "\t  \t ");
            for (int c = 0; c < nbOptions; c++)
            {
                System.out.print(s.getVar(OPT[c][p]).getVal() + " ");
            }
            System.out.println("");
        }

        System.out.println();

        for (int p = 0; p < nbPositions; p++) {
            if(p==0){
                System.out.print("\t\t  ");
            }
            System.out.print(s.getVar(S[p]).getVal() +"  ");
        }
        System.out.println();

        for(int b = 0; b < nbOptions; b++){
            System.out.print((b+1) + " | " + P[b] + "/" + Q[b] + "   ");
            for (int p = 0; p < nbPositions; p++) {
                System.out.print(s.getVar(OPT[b][p]).getVal() + "  ");
            }
            System.out.println();
        }
    }
}
