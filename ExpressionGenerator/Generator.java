import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;

/**
 * <p>
 * Generates expressions for the 2017-2018 CE204 Assignment 2.
 * <br>
 * Change the private static final parameters to get different results.
 * <br>
 * Uncomment the try-catch block in the main method to save the generated expressions to a file
 * instead of printing them to stdout.
 * </p>
 * Created by Ciprian Dascalitei, cd16606@essex.ac.uk
 * <br>
 */
public class Generator {
    //How many expressions to generate
    //For larger numbers, saving to a file is highly recommended
    private static final int EXPRESSIONS_TO_GENERATE = 1;

    //Generates a Part6-compatible expression, where the values of identifiers are manually defined
    private static final boolean GENERATE_PART_6_PREFIX = false;
    //The chance of any specific identifier to be defined
    private static final double DEFINED_IDENTIFIER_CHANCE = 0.5;
    //The maximum amount of defined identifiers
    private static final int DEFINED_IDENTIFIERS_CAP = 2;
    //Whether non-defined identifiers can appear in expressions
    //If this is true and GENERATE_PART_6_PREFIX is false, no identifiers will be used at all
    private static final boolean USE_ONLY_GENERATED_IDENTIFIERS = true;
    //Edge values for identifier nodes (inclusive)
    private static final char FIRST_POSSIBLE_IDENTIFIER = 'A';
    private static final char LAST_POSSIBLE_IDENTIFIER = 'Z';

    //How long each expression should be
    //Unit of measure is number of operators
    private static final int IDENTIFIER_EXP_LENGTH = 3;
    private static final int MAIN_EXP_LENGTH = 8;

    //Edge values for number nodes (inclusive)
    private static final int MIN_NUMBER = 0;
    private static final int MAX_NUMBER = 99;

    //How many open brackets can exist at the same time
    //An open bracket is the '(' character
    private static final int MAX_NESTED_BRACKETS = 3;
    //Larger values mean that brackets encompass a greater numbers of operations
    //Smaller values result in brackets encompassing fewer operations
    private static final double BRACKET_LIFESPAN = 0.6;

    //individual chance = (individual weight) / (sum of all weights)
    private static final double BRACKET_WEIGHT = 0.3;
    private static final double NUMBER_WEIGHT = 1;
    private static final double IDENTIFIER_WEIGHT = 0.5;

    //The operators to be used
    private static final char[] OPERATORS = "+-*/%^".toCharArray();
    //The PRNG to be used
    private static final Random RANDOM = new Random();

    /*
     * BLACK MAGIC FOLLOWS
     *
     * YOU HAVE BEEN WARNED
     */
    private static String generateExpression(int length, boolean useIdentifiers,
                                             ArrayList<Character> availableIdentifiers) {
        boolean wasOp = true;
        boolean wasBracket = false;
        int opCount = 0;
        int openBrackets = 0;
        double roll;

        if (availableIdentifiers != null && availableIdentifiers.size() == 0)
            useIdentifiers = false;

        StringBuilder expressionBuilder = new StringBuilder();

        while (opCount <= length || wasOp) {
            if (wasOp || wasBracket) {
                roll = (BRACKET_WEIGHT + NUMBER_WEIGHT + IDENTIFIER_WEIGHT) * RANDOM.nextDouble();

                if (roll <= BRACKET_WEIGHT) {
                    if (openBrackets < MAX_NESTED_BRACKETS) {
                        wasOp = false;
                        wasBracket = true;

                        openBrackets++;
                        expressionBuilder.append('(');
                    }
                } else {
                    roll -= BRACKET_WEIGHT;

                    if (roll <= NUMBER_WEIGHT) {
                        wasOp = false;
                        wasBracket = false;

                        expressionBuilder.append(makeNumber());
                    } else {
                        roll -= NUMBER_WEIGHT;

                        if (roll <= IDENTIFIER_WEIGHT && useIdentifiers) {
                            wasOp = false;
                            wasBracket = false;

                            expressionBuilder.append(makeId(availableIdentifiers));
                        }
                    }
                }

            } else {
                roll = RANDOM.nextDouble();
                if (openBrackets > 0 && roll > BRACKET_LIFESPAN) {
                    wasOp = false;
                    wasBracket = false;

                    openBrackets--;
                    expressionBuilder.append(')');

                } else {
                    wasOp = true;
                    wasBracket = false;
                    opCount++;

                    expressionBuilder.append(makeOp());
                }
            }
        }

        while (openBrackets > 0) {
            openBrackets--;
            if (expressionBuilder.charAt(expressionBuilder.length() - 1) == '(')
                expressionBuilder.delete(expressionBuilder.length() - 2, expressionBuilder.length());
            else
                expressionBuilder.append(')');
        }

        return expressionBuilder.toString();
    }

    private static char makeOp() {
        return OPERATORS[RANDOM.nextInt(OPERATORS.length)];
    }

    private static char makeId(ArrayList<Character> availableIdentifiers) {
        if (availableIdentifiers == null)
            return (char) (FIRST_POSSIBLE_IDENTIFIER +
                    RANDOM.nextInt(LAST_POSSIBLE_IDENTIFIER - FIRST_POSSIBLE_IDENTIFIER));
        else
            return availableIdentifiers.get(RANDOM.nextInt(availableIdentifiers.size()));
    }

    private static int makeNumber() {
        return MIN_NUMBER + RANDOM.nextInt(MAX_NUMBER - MIN_NUMBER + 1);
    }


    public static void main(String[] args) {
        StringBuilder result = new StringBuilder();
        PrintStream out = System.out;

        try {
            out = new PrintStream(new File("exp.txt"));
        } catch (IOException ex) {
        }


        if (out == null)
            return;

        for (int i = 0; i < EXPRESSIONS_TO_GENERATE; i++) {
            ArrayList<Character> generatedIdentifiers = null;

            if (GENERATE_PART_6_PREFIX) {
                result.append("let ");
                boolean addedId = false;
                generatedIdentifiers = new ArrayList<>();

                for (char id = FIRST_POSSIBLE_IDENTIFIER;
                     id <= LAST_POSSIBLE_IDENTIFIER && generatedIdentifiers.size() < DEFINED_IDENTIFIERS_CAP;
                     id++)
                    if (RANDOM.nextDouble() <= DEFINED_IDENTIFIER_CHANCE) {
                        if (addedId)
                            result.append(" and ");

                        result.append(id);
                        result.append(" = ");
                        result.append(generateExpression(IDENTIFIER_EXP_LENGTH, false, null));

                        generatedIdentifiers.add(id);
                        addedId = true;
                    }

                if (!addedId) {
                    result.delete(0, result.length());
                    generatedIdentifiers = null;
                } else
                    result.append(" in ");
            }

            if (USE_ONLY_GENERATED_IDENTIFIERS)
                result.append(generateExpression(MAIN_EXP_LENGTH,
                        GENERATE_PART_6_PREFIX,
                        generatedIdentifiers));
            else
                result.append(generateExpression(MAIN_EXP_LENGTH,
                        true,
                        null));
            result.append(";");

            out.println(result.toString());
            result.delete(0, result.length());
        }
    }
}
