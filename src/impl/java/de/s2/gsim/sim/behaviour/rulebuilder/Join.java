package de.s2.gsim.sim.behaviour.rulebuilder;

import jess.Context;
import jess.JessException;
import jess.RU;
import jess.Userfunction;
import jess.Value;
import jess.ValueVector;

public class Join implements Userfunction {

    private static final String NAME = "join";

    /** Get the name of the Jess function.
     * @see jess.Userfunction#getName()
     */
    public String getName(){
        return NAME;
    }

    /** Call the userfunction
     * @see jess.Userfunction#call(jess.ValueVector, jess.Context)
     */
    public Value call(ValueVector vv, Context context ) throws JessException {
        // Function rguments are in the ValueVector argument.
        // The first element of this aggregate contains the function name.
        // Check that we have at least one element.
        int narg = vv.size() - 1;
        if( narg < 1 )
            throw new JessException( NAME, "missing argument.", "" );

        // Get and evaluate the separator.
        String separg = vv.get( 1 ).stringValue( context );
        String sep = "";

        // Iterate over the remaining arguments, collect them in a StringBuilder.
        StringBuilder sb = new StringBuilder();
        for( int i = 2; i <= narg; i++ ){
            sb.append( sep );
            sep = separg;
            // This evaluates variables and function calls.
            Value item = new Value( vv.get( i ).resolveValue( context ) );
            if( item.type() == RU.LIST ){
                // Lists need to be "flattened".
                ValueVector iList = item.listValue( context );
                if( iList.size() > 0 ){
                    sb.append( iList.get( 0 ).stringValue( context ) );
                    for( int j = 1; j < iList.size(); j++ ){
                        sb.append( sep ).append( iList.get( j ).stringValue( context ) );
                    }
                }
            } else {
                sb.append( item.stringValue( context ) );
            }
        }

        return new Value( sb.toString(), RU.STRING );
    }
}