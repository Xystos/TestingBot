package bowtie.dict.wrap.urban;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author &#8904
 *
 */
public class UrbanDictionaryWord
{
    private List<UrbanDictionaryDefinition> definitions = new ArrayList<UrbanDictionaryDefinition>();

    public UrbanDictionaryWord(List<UrbanDictionaryDefinition> definitions)
    {
        this.definitions = definitions;
        sort(true);
    }

    public UrbanDictionaryDefinition get(int index)
    {
        return this.definitions.get(index);
    }

    public int getDefinitionCount()
    {
        return this.definitions.size();
    }

    public void sort(final boolean desc)
    {
        Collections.sort(definitions, new Comparator<UrbanDictionaryDefinition>()
        {
            @Override
            public int compare(UrbanDictionaryDefinition def1, UrbanDictionaryDefinition def2)
            {
                double def1rat = (double)def1.getThumbsUp() / (double)def1.getThumbsDown();
                double def2rat = (double)def2.getThumbsUp() / (double)def2.getThumbsDown();

                if (def1rat > def2rat)
                {
                    return desc ? -1 : 1;
                }
                else if (def1rat < def2rat)
                {
                    return desc ? 1 : -1;
                }
                else
                {
                    return 0;
                }
            }
        });

        for (int i = 0; i < definitions.size(); i ++ )
        {
            definitions.get(i).setIndex(i + 1);
            definitions.get(i).setListsize(definitions.size());
        }
    }
}