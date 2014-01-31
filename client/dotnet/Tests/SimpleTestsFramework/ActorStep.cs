using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Tests.SimpleTestsFramework
{
    /// <summary>
    /// Represents a step in a test performed by a named entity (ActorName).
    /// </summary>
    public class ActorStep : Step
    {
        readonly private string actorName;

        public ActorStep(string name, string actorName)
            : base(name)
        {
            this.actorName = actorName;
        }
        public string ActorName
        {
            get { return actorName; }
        }
    }
}
