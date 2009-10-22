using System;
using System.Threading;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Tests.SimpleTestsFramework
{
    public abstract class Test
    {
        private readonly string name;
        
        private IList<PreRequisite> preRequisites = new List<PreRequisite>();
        private MainAction action;
        private IList<Consequence> consequences = new List<Consequence>();
        private IList<Epilogue> epilogues = new List<Epilogue>();

        private static int defaultTimeout = 3 * 1000;

        private volatile int timeout = DefaultTimeout;
        private volatile bool canSkipTest = false;


        public Test(string name)
        {
            this.name = name;
        }

        public bool CanSkipTest
        {
            get { lock (this)return canSkipTest; }
            set { lock (this)canSkipTest = value; }
        }

        public int Timeout
        {
            get { lock (this)return timeout; }
            set { lock (this)timeout = value; }
        }

        public string Name
        {
            get { lock (this)return name; }
        }

        public static int DefaultTimeout
        {
            get
            {
                lock (typeof(Test))
                {
                    return defaultTimeout;
                }
            }
            set
            {
                lock (typeof(Test))
                {
                    defaultTimeout = value;
                }
            }
        }

        public void AddPrequisite(PreRequisite prerequisite)
        {
            lock(this)
                preRequisites.Add(prerequisite);
        }

        public void SetAction(MainAction action)
        {
            lock (this)
                this.action = action;
        }
        public void AddConsequence(Consequence consequence)
        {
            lock (this)
                consequences.Add(consequence);
        }

        public void AddEpilogue(Epilogue epilogue)
        {
            lock (this)
                epilogues.Add(epilogue);
        }

        public bool Run(int numberOfRuns)
        {
            bool result = true;
		    if (this.CanSkipTest)
		    {
			    Console.WriteLine("Test skiped");
			    return true;
		    }
             Console.WriteLine("Building test - " + Name);
		    Build();

		    Console.WriteLine("Initializing  test - " + Name);
		    foreach (PreRequisite prereq in preRequisites)
		    {
			    if( prereq.Call().IsFailure() )
                {
                    Console.WriteLine("##PreRequisite execution failed - " + prereq.Name);
                }
		    }

		    Console.WriteLine("Performing test - " + Name);
            
            WaitHandle[] handles = new WaitHandle[consequences.Count + 1];
            int index = 0;

            foreach (Consequence consequence in consequences)
            {
                Consequence tmpConsequence = consequence; // if you know closures you understand this.
                ThreadPool.QueueUserWorkItem(
                    (o) =>
                    {
                        tmpConsequence.Call();
                    }
                );

                handles[index++] = consequence.ManualResetEvent;
            }

            ThreadPool.QueueUserWorkItem( 
                (o) => {
                    action.Call();
                }
            );
            handles[index] = action.ManualResetEvent;
                        

            bool res = WaitHandle.WaitAll(handles, this.Timeout , false);


            if (!res)
            {
                Console.WriteLine("### Action or consquences timedout.");
            }

            if (!DisplayStepResult(action))
                result = false;
            
            
            foreach (Consequence consequence in consequences)
		    {
                if (!DisplayStepResult(consequence))
                    result = false;
            }

		   Console.WriteLine("Finalizing test - " + Name);

           foreach (Epilogue epilogue in epilogues)
           {
               if (epilogue.Call().IsFailure())
               {
                   Console.WriteLine("##Epilogue execution failed - " + epilogue.Name);
               }
           }

            return result;
        }

        private bool DisplayStepResult(Step step)
        {
            bool result = false;
            if (!step.Done)
            {
                Console.WriteLine("##Step '" + step.Name + "' didn't complete.");
            }
            else
            {
                if (!step.Sucess)
                {
                    Console.WriteLine("##Step '" + step.Name + "' failed. Reason: " + step.ReasonForFailure);
                }
                else
                {
                    Console.WriteLine("Step '" + step.Name + "' finished.");
                    result = true;
                }
            }
            return result;
        }

        public abstract void Build();
    }
}
