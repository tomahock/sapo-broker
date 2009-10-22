using System;
using System.Threading;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Tests.SimpleTestsFramework
{
    /// <summary>
    /// Represents a step in a test.
    /// </summary>
    public abstract class Step
    {

        readonly private string name;
        volatile private string reasonForFailure;
        readonly private ManualResetEvent manualResetEvent;
        volatile private System.Action run;

        volatile private bool done = false;
        volatile private bool sucess = false;

        public ManualResetEvent ManualResetEvent
        {
            get { return manualResetEvent; }
        }

        public Step(String name)
        {
            this.name = name;
            manualResetEvent = new ManualResetEvent(false);
        }

        public String Name
        {
            get { return name; }
        }

        public bool Done
        {
            get { return done; }
            set { 
                this.done = value;
                manualResetEvent.Set();
            }
        }

        public bool Sucess
        {
            get { return sucess; }
            set { this.sucess = value; }
        }

        public bool IsFailure()
        {
            return (Done) && (!Sucess);
        }

        public string ReasonForFailure
        {
            set
            {
                Sucess = false;
                this.reasonForFailure = value;
                Done = true;
            }
            get
            {
                return this.reasonForFailure;
            }
        }

        public Step Call()
        {
            if (IsFailure())
            {
                return this;
            }
            try
            {
                if (run != null)
                    run();
            }
            catch (Exception e)
            {
                this.ReasonForFailure = "Exception - " + e.Message;
            }

            return this;
        }

        public System.Action Runnable
        {
            get { return run; }
            set { run = value; }
        }
    }
}
